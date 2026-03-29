import os

files_to_extract = {
    'finalexam.py': 'data_final.py',
    'masterleveltest.py': 'data_master.py',
    'placementest1.py': 'data_placement1.py',
    'placementest2.py': 'data_placement2.py',
    'placementest3.py': 'data_placement3.py',
    'ِadvancedleveltest.py': 'data_advanced.py',
    'pro_leveltest (1).py': 'data_pro.py'
}

cwd = r"c:\Users\mezoh\Desktop\model"
api_dir = os.path.join(cwd, "api")

for src, dst in files_to_extract.items():
    src_path = os.path.join(cwd, src)
    dst_path = os.path.join(api_dir, dst)
    
    try:
        with open(src_path, 'r', encoding='utf-8') as f:
            content = f.read()
    except Exception as e:
        print(f"Error reading {src.encode('utf-8')}: {e}")
        continue
        
    parts = content.split("quiz_data = [")
    if len(parts) > 1:
        data_part = parts[1]
        end_idx = data_part.find("JS_CODE =")
        if end_idx != -1:
            # backtrack to the closing bracket
            last_bracket = data_part.rfind("]", 0, end_idx)
            if last_bracket != -1:
                quiz_content = data_part[:last_bracket+1]
                with open(dst_path, 'w', encoding='utf-8') as out:
                    out.write("quiz_data = [" + quiz_content + "\n")
                    print(f"Extracted to {dst}")
            else:
                print(f"Failed to find closing bracket in {src.encode('utf-8')}")
        else:
             print(f"Failed to find JS_CODE in {src.encode('utf-8')}")
    else:
        print(f"Failed to find quiz_data in {src.encode('utf-8')}")
