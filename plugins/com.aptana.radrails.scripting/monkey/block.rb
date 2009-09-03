=begin
 Menu: Ruby > Toggle Block
 Kudos: Christopher Williams
 Key: M1+Shift+[
 License: EPL 1.0
 DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.ruby
=end

editor = $editors.get_active_editor
selection = editor.selection_range

# converts do |x| .. end to {|x| }
def convert_do_end_to_braces(editor, selection)
  src = editor.source[selection]
  match = src.match(/\s*do\s*(\|[^|]*\|)?(.*)\s*end/m)
  return false if match.nil? || match.size != 3
  var_with_pipes = match[1]
  body_lines = match[2].split("\n") # break into lines
  body_lines = body_lines.map {|line| line.strip } # strip leading/trailing whitespace
  body_lines = body_lines.delete_if {|line| line.size == 0 } # Remove empty lines of body
  squished_body = body_lines.join("; ")
  modified_src = src[0...-match[0].size] + "{" + var_with_pipes + " " + squished_body + " }"
  editor.apply_edit(selection.first, (selection.last - selection.first), modified_src)
  return true
end

# converts {|x| } to do |x| .. end
def convert_braces_to_do_end(editor, selection)
  src = editor.source[selection]
  match = src.match(/\s*\{\s*(\|[^|]*\|)?(.*)\s*\}/m)
  return if match.nil? || match.size != 3
  var_with_pipes = match[1]
  body_lines = match[2].split(";") # break into lines
  body_lines = body_lines.map {|line| "  " + line.strip } # strip leading/trailing whitespace
  body_lines = body_lines.delete_if {|line| line.strip.size == 0 } # Remove empty lines of body
  squished_body = body_lines.join("\n")
  modified_src = src[0...-match[0].size] + "do " + var_with_pipes + "\n" + squished_body + "\nend"
  editor.apply_edit(selection.first, (selection.last - selection.first), modified_src)
end

convert_braces_to_do_end(editor, selection) if (!convert_do_end_to_braces(editor, selection))
