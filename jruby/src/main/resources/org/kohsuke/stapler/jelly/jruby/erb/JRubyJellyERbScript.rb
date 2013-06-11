require 'erb'
require 'java'

module JRubyJellyScriptImpl
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

      # ERB calls this during initialization
      def force_encoding(enc)
        self
      end
    end
  end
end