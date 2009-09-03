=begin
 Menu: Ruby > Make Destructive
 Key: M1+Shift+1
 Kudos: James Edward Gray II, Christopher Williams
 License: EPL 1.0
 DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.ruby
=end

# Adapted from James Edward Gray's Ruby Idioms Textmate bundle

def get_start_of_line
  editor = $editors.get_active_editor
  offset = editor.current_offset
  before = editor.source[0...offset]
  last_index = before.rindex("\n")
  start = offset
  if !last_index.nil?
    start = last_index + 1
  end
  return start
end

def get_current_line
  editor = $editors.get_active_editor
  line = editor.source[get_start_of_line..-1]
  index = line.index("\n")
  line = line[0..index] if !index.nil?
  return line
end

line = get_current_line
original_length = line.length
line.sub!(/\b(chomp|chop|collect|compact|delete|downcase|exit|flatten|gsub|lstrip|map|next|reject|reverse|rstrip|slice|sort|squeeze|strip|sub|succs|swapcase|tr|tr_s|uniq|upcase)\b(?!\!)/, "\\1!")
line.sub!(/[$`\\]/, '\\\\\&')
if line.length != original_length # if we made any changes to the line, then apply them
  editor = $editors.get_active_editor
  editor.apply_edit(get_start_of_line, original_length, line) 
end