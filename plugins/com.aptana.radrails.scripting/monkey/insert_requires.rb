=begin
 Menu: Ruby > Insert Requires
 Kudos: James Edward Gray II, Christopher Williams
 Key: M1+R
 License: EPL 1.0
 DOM: http://download.eclipse.org/technology/dash/update/org.eclipse.eclipsemonkey.lang.ruby
=end

# Adapted from James Edward Gray's Ruby Idioms Textmate bundle
def build_requires( code, libs )
  libs.reject { |lib| code =~ /require\s*(['"])#{lib}\1/ }.map { |lib| "require \"#{lib}\"\n" }.join
end

def place_requires( code, new_reqs )
  return code unless new_reqs =~ /\S/
  
  code.dup.sub!(/(?:^[ \t]*require\s*(['"]).+?\1.*\n)+/, "\\&#{new_reqs}") ||
  code.sub(/\A(?:\s*(?:#.*)?\n)*/, "\\&#{new_reqs}\n")
end

REQUIRES = { "abbrev"           => [/\babbrev\b/],
                 "base64"           => [/\bBase64\b/],
                 "benchmark"        => [/\bBenchmark\b/],
                 "bigdecimal"       => [/\bBigDecimal\b/],
                 "bigdecimal/math"  => [/\bBigMath\b/],
                 "cgi"              => [/\bCGI\b/],
                 "complex"          => [/\bComplex\b/],
                 "csv"              => [/\bCSV\b/],
                 "curses"           => [/\bCurses\b/],
                 "date"             => [/\bDate(?:Time)?\b/],
                 "dbm"              => [/\bDBM\b/],
                 "delegate"         => [ /\bDelegateClass\b/, /\bDelegator\b/,
                                         /\bSimpleDelegator\b/ ],
                 "digest"           => [/\bMD5\b/, /\bSHA1\b/],
                 "dl"               => [/\bDL\b/],
                 "enumerator"       => [ /\b(?:enum|each)_(?:cons|slice)\b/,
                                         /\benum_(?:for|with_index)\b/,
                                         /\bto_enum\b/ ],
                 "erb"              => [/\bERB\b/],
                 "etc"              => [/\bEtc\b/],
                 "fcntl"            => [/\bFcntl\b/],
                 "fileutils"        => [/\bFileUtils\b/],
                 "find"             => [/\bFind(?:\.|::)find\b/],
                 "forwardable"      => [/\b(?:Single)?Forwardable\b/],
                 "gdbm"             => [/\bGDBM\b/],
                 "generator"        => [/\bGenerator\b/, /\bSyncEnumerator\b/],
                 "getoptlong"       => [/\bGetoptLong\b/],
                 "gserver"          => [/\bGServer\b/],
                 "iconv"            => [/\bIconv\b/],
                 "ipaddr"           => [/\bIpAddr\b/],
                 "logger"           => [/\bLogger\b/],
                 "matrix"           => [/\bMatrix\b/, /\bVector\b/],
                 "monitor"          => [/\bMonitor(?:Mixin)?\b/],
                 "net/ftp"          => [/\bNet::FTP\b/],
                 "net/http"         => [/\bNet::HTTP\b/],
                 "net/imap"         => [/\bNet::IMAP\b/],
                 "net/pop"          => [/\bNet::(?:APOP|POP3)\b/],
                 "net/smtp"         => [/\bNet::SMTP\b/],
                 "net/telnet"       => [/\bNet::Telnet\b/],
                 "nkf"              => [/\bNKF\b/],
                 "observer"         => [/\bObservable\b/],
                 "open2"            => [/\bOpen3\b/],
                 "optparse"         => [/\bOptionParser\b/],
                 "ostruct"          => [/\bOpenStruct\b/],
                 "pathname"         => [/\bPathname\b/],
                 "ping"             => [/\bPing\b/],
                 "pp"               => [/\bpp\b/],
                 "pstore"           => [/\bPStore\b/],
                 "rational"         => [/\bRational\b/],
                 "rdoc/usage"       => [/\bRDoc(?:\.|::)usage\b/],
                 "rdoc/markup/simple_markup" \
  => [/\bSM::SimpleMarkup\b/],
                 "rdoc/markup/simple_markup/to_html" \
  => [/\bSM::SimpleMarkup\b/],
                 "rdoc/usage"       => [/\bRDoc(?:\.|::)usage\b/],
                 "resolv"           => [/\bResolv\b/],
                 "rexml/document"   => [/\bREXML\b/],
                 "rinda/tuplespace" => [/\bRinda::TupleSpace(?:Proxy)?\b/],
                 "rinda/ring"       => [/\bRinda::Ring(?:Finger|Server)?\b/],
                 "rss"              => [/\bRSS\b/],
                 "scanf"            => [/\bscanf\b/],
                 "sdbm"             => [/\bSDBM\b/],
                 "set"              => [/\b(?:Sorted)?Set\b/],
                 "singleton"        => [/\bSingleton\b/],
                 "soap"             => [/\bSOAP\b/],
                 "socket"           => [ /\b(?:TCP|UNIX)(?:Socket|Server)\b/,
                                         /\b(?:UDP)?Socket\b/ ],
                 "stringio"         => [/\bStringIO\b/],
                 "strscan"          => [/\bStringScanner\b/],
                 "syslog"           => [/\bSyslog\b/],
                 "tempfile"         => [/\bTempfile\b/],
                 "test/unit"        => [/\bTest::Unit\b/],
                 "thread"           => [ /\bConditionVariable\b/, /\bMutex\b/,
                                         /\b(?:Sized)?Queue\b/ ],
                 "time"             => [/\bTime(?:\.|::)parse\b/],
                 "timeout"          => [/\bTimeout(?:\.|::)timeout\b/],
                 "tk"               => [/\bTK\b/],
                 "tmpdir"           => [/\bDir(?:\.|::)tmpdir\b/],
                 "tracer"           => [/\bTracer\b/],
                 "tsort"            => [/\bTSort\b/],
                 "uri"              => [/\bURI\b/],
                 "weakref"          => [/\bWeakRef\b/],
                 "webrick"          => [/\bWEBrick\b/],
                 "Win32API"         => [/\bWin32(?:API)?\b/],
                 "win32ole"         => [/\bWIN32OLE\b/],
                 "wsdl"             => [/\bWSDL\b/],
                 "xmlrpc"           => [/\bXMLRPC\b/],
                 "yaml"             => [/\bYAML\b/],
                 "zlib"             => [/\bZlib\b/] }

editor = $editors.get_active_editor
code = editor.source
libs = REQUIRES.select { |lib, usage| usage.any? { |test| code =~ test } }.map { |kv| kv.first }
add = build_requires(code, libs).strip
if add.length != 0
  add += "\n"  
  editor.apply_edit(0, 0, add)
end