=begin
 Menu: Ruby > String to Symbol
 Kudos: Christopher Williams
 Key: M1+Shift+;
 License: EPL 1.0
 DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.ruby
=end

# If the current token is a string containing alnums, change it to
# a symbol
editor = $editors.get_active_editor
selection = editor.selection_range
selected_src = editor.source[selection]
# if entire selection is a string with no dynamic content, then convert the string to a symbol
match = selected_src.match(/(['"])([_a-zA-Z][_\w]*)\1/)
return if match.nil? || match.size != 3
symbol = ":" + match[2]
editor.apply_edit(selection.first, (selection.last - selection.first), symbol)
