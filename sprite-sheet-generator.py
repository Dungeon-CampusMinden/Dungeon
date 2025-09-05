import os
import math
import argparse
import json
from PIL import Image

def create_animation_config(x, y, width, height, rows, columns, sprite_width, sprite_height):
    return {
        "config": {
            "spriteWidth": sprite_width,
            "spriteHeight": sprite_height,
            "x": x,
            "y": y,
            "rows": rows,
            "columns": columns
        },
        "framesPerSprite": 10,
        "scaleX": 1,
        "scaleY": 0,
        "isLooping": True,
        "centered": False
    }

def write_json_config(configs, output_path):
    with open(output_path, "w") as f:
        json.dump(configs, f, indent=2)
    print(f"Animation config written to: {output_path}")

def get_uniform_sprite_size(images):
    widths, heights = zip(*(img.size for img in images))
    if len(set(widths)) > 1 or len(set(heights)) > 1:
        return None
    return widths[0], heights[0]

def create_sprite_sheet(folder_path, output_path, stack=False, allow_variable_sizes=False, animation_name="idle"):
    image_files = sorted([
        f for f in os.listdir(folder_path)
        if f.lower().endswith(('.png', '.jpg', '.jpeg'))
    ])

    if not image_files:
        print(f"No image files found in: {folder_path}")
        return False, None

    images = [Image.open(os.path.join(folder_path, f)) for f in image_files]
    num_images = len(images)

    if not allow_variable_sizes:
        sprite_size = get_uniform_sprite_size(images)
        if sprite_size is None:
            print(f"Error: Not all images in {folder_path} have the same dimensions. Use --stack to allow variable sizes.")
            return False, None
        sprite_width, sprite_height = sprite_size

    if stack or allow_variable_sizes:
        max_width = max(img.width for img in images)
        total_height = sum(img.height for img in images)
        sheet = Image.new('RGBA', (max_width, total_height))
        y_offset = 0
        for img in images:
            sheet.paste(img, (0, y_offset))
            y_offset += img.height

        if not allow_variable_sizes:
            config = {
                animation_name: create_animation_config(
                    x=0, y=0,
                    width=max_width, height=total_height,
                    rows=num_images, columns=1,
                    sprite_width=sprite_width, sprite_height=sprite_height
                )
            }
        else:
            config = None
    else:
        sprite_width, sprite_height = images[0].size
        if num_images <= 8:
            cols = num_images
            rows = 1
        else:
            cols = 8
            rows = math.ceil(num_images / 8)

        sheet_width = cols * sprite_width
        sheet_height = rows * sprite_height
        sheet = Image.new('RGBA', (sheet_width, sheet_height))

        for index, img in enumerate(images):
            x = (index % cols) * sprite_width
            y = (index // cols) * sprite_height
            sheet.paste(img, (x, y))

        config = {
            animation_name: create_animation_config(
                x=0, y=0,
                width=sheet_width, height=sheet_height,
                rows=rows, columns=cols,
                sprite_width=sprite_width, sprite_height=sprite_height
            )
        }

    sheet.save(output_path)
    print(f"Saved: {output_path}")
    return True, config

def process_multi_mode(parent_folder):
    subfolders = [
        os.path.join(parent_folder, name)
        for name in sorted(os.listdir(parent_folder))
        if os.path.isdir(os.path.join(parent_folder, name))
    ]

    intermediate_paths = []
    config_data = {}
    y_offset = 0

    for subfolder in subfolders:
        name = os.path.basename(subfolder)
        output_path = os.path.join(parent_folder, f"{name}.png")
        success, config = create_sprite_sheet(
            folder_path=subfolder,
            output_path=output_path,
            stack=False,
            allow_variable_sizes=False,
            animation_name=name
        )
        if success:
            intermediate_paths.append((output_path, name, config[name]))

    if not intermediate_paths:
        print("No valid subfolders processed.")
        return

    images = [Image.open(p[0]) for p in intermediate_paths]
    max_width = max(img.width for img in images)
    total_height = sum(img.height for img in images)
    final_image = Image.new('RGBA', (max_width, total_height))

    for (img_path, anim_name, anim_cfg), img in zip(intermediate_paths, images):
        final_image.paste(img, (0, y_offset))
        anim_cfg['config']['x'] = 0
        anim_cfg['config']['y'] = y_offset
        config_data[anim_name] = anim_cfg
        y_offset += img.height

    folder_basename = os.path.basename(os.path.abspath(parent_folder))
    final_output_path = os.path.join(parent_folder, f"{folder_basename}.png")
    final_json_path = os.path.join(parent_folder, f"{folder_basename}.json")

    final_image.save(final_output_path)
    write_json_config(config_data, final_json_path)

    # Clean up intermediate images
    for (img_path, _, _) in intermediate_paths:
        try:
            os.remove(img_path)
            print(f"Deleted intermediate: {img_path}")
        except Exception as e:
            print(f"Failed to delete {img_path}: {e}")

def unpack_spritesheet(base_path):
    image_path = base_path + ".png"
    json_path = base_path + ".json"

    if not os.path.exists(image_path) or not os.path.exists(json_path):
        print(f"Error: Missing .png or .json for base path: {base_path}")
        return

    with open(json_path, "r") as f:
        config_data = json.load(f)

    sheet = Image.open(image_path)

    for anim_name, anim_cfg in config_data.items():
        cfg = anim_cfg["config"]
        sprite_width = cfg["spriteWidth"]
        sprite_height = cfg["spriteHeight"]
        x_start = cfg["x"]
        y_start = cfg["y"]
        rows = cfg["rows"]
        cols = cfg["columns"]

        output_dir = os.path.join(os.path.dirname(base_path), anim_name)
        os.makedirs(output_dir, exist_ok=True)

        frame_index = 0
        for row in range(rows):
            for col in range(cols):
                frame_index += 1
                left = x_start + col * sprite_width
                top = y_start + row * sprite_height
                right = left + sprite_width
                bottom = top + sprite_height
                frame = sheet.crop((left, top, right, bottom))
                out_path = os.path.join(output_dir, f"{anim_name}_{frame_index:02d}.png")
                frame.save(out_path)
        print(f"Extracted {frame_index} frames for animation '{anim_name}' into {output_dir}")

# CLI
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate or unpack a sprite sheet and config from a folder of images.")
    parser.add_argument("folder", help="Path to the folder (or base path if --unpack)")
    parser.add_argument("--stack", action="store_true", help="Stack images vertically (allows varying sizes)")
    parser.add_argument("--single", action="store_true", help="Only process a single folder of images (no subfolders)")
    parser.add_argument("--unpack", action="store_true", help="Unpack a sprite sheet (.png + .json) into individual frames")
    args = parser.parse_args()

    if args.unpack:
        unpack_spritesheet(args.folder)
    elif args.single:
        folder_name = os.path.basename(os.path.abspath(args.folder))
        parent_folder = os.path.dirname(os.path.abspath(args.folder))
        image_files = [
            f for f in os.listdir(args.folder)
            if f.lower().endswith(('.png', '.jpg', '.jpeg'))
        ]
        if not image_files:
            print(f"No image files found in: {args.folder}")
        else:
            first_ext = os.path.splitext(image_files[0])[1].lower().lstrip('.')
            output_filename = f"{folder_name}.{first_ext}"
            output_path = os.path.join(parent_folder, output_filename)
            json_path = os.path.join(parent_folder, f"{folder_name}.json")
            success, config = create_sprite_sheet(
                folder_path=args.folder,
                output_path=output_path,
                stack=args.stack,
                allow_variable_sizes=args.stack,
                animation_name="idle"
            )
            if success and config:
                write_json_config(config, json_path)
    else:
        process_multi_mode(args.folder)
