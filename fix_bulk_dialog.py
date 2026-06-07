import re
from pathlib import Path

path = Path('src/ui/BulkImportDialog.java')
text = path.read_text(encoding='utf-8')

# Find and fix the malformed line - the setSelectedFile line is on line 245 and missing proper newline/tabs
# The problem is: fileChooser.setSelectedFile(new File("product_import_template.csv"));					int result = ...
# Should be properly on separate lines

# First, let's find the problematic pattern
bad_pattern = r'fileChooser\.setSelectedFile\(new File\("product_import_template\.csv"\)\);(\s+)int result = fileChooser\.showSaveDialog'

if re.search(bad_pattern, text):
    # Replace with properly formatted version
    text = re.sub(
        bad_pattern,
        'fileChooser.setSelectedFile(new File("product_import_template.csv"));\n\t\t\t\t\tint result = fileChooser.showSaveDialog',
        text
    )
    path.write_text(text, encoding='utf-8')
    print('Fixed line formatting in BulkImportDialog.java')
else:
    print('Pattern not found - trying alternative approach')
    # Try to find just the problematic area
    if 'fileChooser.setSelectedFile(new File("product_import_template.csv"));' in text and 'int result = fileChooser.showSaveDialog' in text:
        # Replace with correct version
        old_text = '''fileChooser.setSelectedFile(new File("product_import_template.csv"));					int result = fileChooser.showSaveDialog(BulkImportDialog.this);
					if (result == JFileChooser.APPROVE_OPTION) {'''
        new_text = '''fileChooser.setSelectedFile(new File("product_import_template.csv"));
					int result = fileChooser.showSaveDialog(BulkImportDialog.this);

					if (result == JFileChooser.APPROVE_OPTION) {'''
        
        if old_text in text:
            text = text.replace(old_text, new_text)
            path.write_text(text, encoding='utf-8')
            print('Fixed using alternative approach')
        else:
            print('Could not find exact pattern to replace')
    else:
        print('Missing key components')
