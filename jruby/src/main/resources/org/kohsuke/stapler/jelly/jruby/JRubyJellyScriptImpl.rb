require 'erb'
require 'java'

module JRubyJellyScriptImpl

  class OutputStream
    attr_accessor :output

    def initialize(output)
      @output = output
    end
  end

  class JRubyContext
    include org::kohsuke::stapler::jelly::jruby::IJRubyContext

    attr_accessor :script, :context, :stream

    def initialize(script, context, stream)
      @script = script
      @context = context
      @stream = stream
    end

    def evaluate(&block)
      yield(WriterBinding.new(self).instance_eval{binding})
    end

    def getJellyContext()
      @context
    end

    def setJellyContext(context)
      @context = context
    end

    def getOutput()
      @stream.output
    end

    def setOutput(output)
      @stream.output = output
    end
  end

  # variables exposed to ERB
  class WriterBinding
    def initialize(context)
      @context = context
    end

    def context
      @context.context
    end

    def stream
      @context.stream
    end

    # this is where we handle all the variable references
    def method_missing(name, *args)
      # variables defined in the current context
      v = context.getVariable(name.to_s)
      return v if v!=nil
      super # make it fail
    end

    # load taglib
    def taglib(uri)
      # TODO: cache
      Taglib.new(@context, uri)
    end
  end

  # receives tag invocations as method calls
  class Taglib
    def initialize(context, uri)
      @context = context
      @uri = uri
    end

    # this is the actual taglib invocation like f.entry(:a => b, :c => d)
    def method_missing(name, *args, &block)
      @context.script.invokeTaglib(@context, @context.context, @context.stream.output, @uri, name.to_s, args[0], block)
    end
  end

  class JRubyJellyERbScript < org::kohsuke::stapler::jelly::jruby::JRubyJellyScript
    def initialize(template)
      super()
      @engine = ERB.new(template, nil, nil, "stream.eoutvar")
    end

    def run(jelly_context,xml_output)
      begin
        ctx = JRubyContext.new(self, jelly_context, ERbOutputStream.new(xml_output))
        ctx.evaluate { |b|
          @engine.result(b)
        }
      rescue Exception => ex
        raise org.apache.commons.jelly.JellyTagException.new(ex.message)
      end
    end

    class ERbOutputStream < OutputStream
      attr_accessor :output

      def initialize(output)
        super(output)
      end

      # ERB calls 'eoutvar.concat(str)'. Return self for intercepting 'concat' call.
      def eoutvar
        self
      end

      # ERB calls "eoutvar = ''" first. Ignore it and just use 'concat' calls.
      def eoutvar=(eoutvar)
        # ignore
      end

      # ERB calls this method every time it wants to write some string
      def concat(str)
        output.write(str) if str
      end
    end
  end

end

