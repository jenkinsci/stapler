# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{haml}
  s.version = "3.1.1"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = [%q{Nathan Weizenbaum}, %q{Hampton Catlin}]
  s.date = %q{2011-04-25}
  s.description = %q{      Haml (HTML Abstraction Markup Language) is a layer on top of XHTML or XML
      that's designed to express the structure of XHTML or XML documents
      in a non-repetitive, elegant, easy way,
      using indentation rather than closing tags
      and allowing Ruby to be embedded with ease.
      It was originally envisioned as a plugin for Ruby on Rails,
      but it can function as a stand-alone templating engine.
}
  s.email = %q{haml@googlegroups.com}
  s.executables = [%q{haml}, %q{html2haml}]
  s.files = [%q{bin/haml}, %q{bin/html2haml}]
  s.homepage = %q{http://haml-lang.com/}
  s.require_paths = [%q{lib}]
  s.rubyforge_project = %q{haml}
  s.rubygems_version = %q{1.8.4}
  s.summary = %q{An elegant, structured XHTML/XML templating engine.}

  if s.respond_to? :specification_version then
    s.specification_version = 3

    if Gem::Version.new(Gem::VERSION) >= Gem::Version.new('1.2.0') then
      s.add_development_dependency(%q<yard>, [">= 0.5.3"])
      s.add_development_dependency(%q<maruku>, [">= 0.5.9"])
    else
      s.add_dependency(%q<yard>, [">= 0.5.3"])
      s.add_dependency(%q<maruku>, [">= 0.5.9"])
    end
  else
    s.add_dependency(%q<yard>, [">= 0.5.3"])
    s.add_dependency(%q<maruku>, [">= 0.5.9"])
  end
end
