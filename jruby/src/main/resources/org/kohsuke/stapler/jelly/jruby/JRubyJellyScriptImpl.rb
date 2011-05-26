require 'erb'
require 'java'

class JRubyJellyScriptImpl < org::kohsuke::stapler::jelly::jruby::JRubyJellyScript
  def initialize(template)
    super()
    @template = ERB.new(template,nil,nil,"self.output")
  end

  def run(jelly_context,xml_output)
    begin
      JRubyContext.new(self,jelly_context,xml_output).evaluate_template(@template)
    rescue Exception => ex
      raise org.apache.commons.jelly.JellyTagException.new(ex.message)
    end
  end

  class JRubyContext
    include org::kohsuke::stapler::jelly::jruby::IJRubyContext

    attr_accessor :context,:xml,:script

    def initialize(script,context,xml)
      @script = script
      @context = context
      @xml = xml
    end

    def evaluate_template(erb)
      erb.run(WriterBinding.new(self).instance_eval{binding})
    end

    def getJellyContext()
      @context;
    end

    def setJellyContext(context)
      @context = context
    end

    def getOutput()
      @output
    end

    def setOutput(output)
      @output = output
    end
  end

  # variables exposed to ERB
  class WriterBinding
    attr_reader :output

    def initialize(context)
      @output = self
      @context = context
    end

    def output=(_)
      # ignoring the assignment, because we want our concat method to be invoked
    end

    def context()
      @context.context
    end

    # this is where we handle all the variable references
    def method_missing(name,*args)
      # variables defined in the current context
      v = context.getVariable(name.to_s)
      return v if v!=nil

      super # make it fail
    end

    # load taglib
    def taglib(uri)
      # TODO: cache
      Taglib.new(@context,uri)
    end

    # ERB calls this method every time it wants to write some string
    def concat(str)
      @context.xml.write(str) if str!=nil
    end
  end

  # receives tag invocations as method calls
  class Taglib
    def initialize(context,uri)
      @context = context
      @uri = uri
    end

    # this is the actual taglib invocation like f.entry(:a => b, :c => d)
    def method_missing(name,*args,&block)
      @context.script.invokeTaglib(@context, @context.context, @context.xml, @uri, name.to_s, args[0], block)
    end
  end
end

