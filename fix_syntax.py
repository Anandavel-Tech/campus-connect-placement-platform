import os
import codecs

def fix_file(filepath):
    # Read with utf-8-sig to automatically strip BOM, and write back as utf-8
    with codecs.open(filepath, 'r', encoding='utf-8-sig') as f:
        content = f.read()
    
    # Strip any invisible garbage that might cause issues just in case
    
    # Check braces count
    open_braces = content.count('{')
    close_braces = content.count('}')
    
    if open_braces > close_braces:
        # Add missing closing braces
        missing = open_braces - close_braces
        content += '\n' + '}' * missing + '\n'
    
    with codecs.open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

src_dir = 'src'
for root, dirs, files in os.walk(src_dir):
    for name in files:
        if name.endswith('.java'):
            fix_file(os.path.join(root, name))

print("Fixed syntax and BOM.")
