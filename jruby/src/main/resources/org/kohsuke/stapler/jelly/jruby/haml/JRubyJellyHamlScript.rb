require 'haml'
require 'java'

module JRubyJellyScriptImpl
  class JRubyJellyHamlScript < org::kohsuke::stapler::jelly::jruby::JRubyJellyScript
    def initialize(template)
      super()
      @engine = Haml::Engine.new("- _hamlout.buffer = stream\n" + template)
    end

    def run(jelly_context,xml_output)
      begin
        ctx = JRubyContext.new(self, jelly_context, HamlOutputStream.new(xml_output))
        ctx.evaluate { |b|
          @engine.render(b)
        }
      rescue Exception => ex
        raise org.apache.commons.jelly.JellyTagException.new(ex.message)
      end
    end

    class HamlOutputStream < OutputStream
      attr_accessor :output

      def initialize(output)
        super(output)
      end

      def <<(str)
        output.write(str) if str
      end
    end
  end
end