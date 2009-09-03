unit_tests = Dir['test/unit/**/*_test.rb']
unit_tests.each { |f| load f; }