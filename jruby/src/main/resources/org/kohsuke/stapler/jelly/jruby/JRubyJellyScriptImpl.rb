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

  # variables exposed to template engine
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
      v = context.getVariableWithDefaultValue(name.to_s,UNDEFINED)
      return v if v!=UNDEFINED
      super # make it fail
    end

    def respond_to?(name)
      if super
        true
      else
        context.getVariableWithDefaultValue(name.to_s, UNDEFINED) != UNDEFINED
      end
    end

    # load taglib
    def taglib(uri)
      # TODO: cache
      Taglib.new(@context, uri)
    end

    UNDEFINED = Object.new
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
end

