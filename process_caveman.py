"""
HD Quality Caveman Sprite Processor
Removes white/checkered background while preserving maximum quality
Uses advanced alpha blending for smooth edges
"""

from PIL import Image
import numpy as np

def remove_background_hd(image_path, output_path):
    """
    High-quality background removal with edge smoothing
    Preserves HD quality and creates smooth transparent edges
    """
    # Load the original image in highest quality
    img = Image.open(image_path)
    
    # Convert to RGBA if not already
    if img.mode != 'RGBA':
        img = img.convert('RGBA')
    
    data = np.array(img, dtype=np.float32)  # Use float for better precision
    
    width, height = img.size
    print(f"Original HD image size: {width}x{height}")
    
    # Create output array
    result = np.zeros((height, width, 4), dtype=np.float32)
    result[:, :, :3] = data[:, :, :3]  # Copy RGB
    result[:, :, 3] = 255  # Start with full opacity
    
    # White/light background removal with gradient alpha
    # More sophisticated approach for better edge quality
    white_threshold = 235  # Lower threshold for more aggressive removal
    gradient_range = 40     # Range for smooth alpha transition
    
    pixels_modified = 0
    
    for y in range(height):
        for x in range(width):
            r, g, b = data[y, x, :3]
            
            # Calculate brightness (perceived luminance)
            brightness = 0.299 * r + 0.587 * g + 0.114 * b
            
            # Calculate how "white" the pixel is
            color_variance = np.std([r, g, b])  # Low variance = uniform color (likely background)
            
            # If pixel is bright AND has low color variance, it's likely background
            if brightness >= white_threshold - gradient_range:
                if color_variance < 15:  # Very uniform color
                    # Calculate alpha based on brightness (smooth gradient)
                    alpha_factor = (brightness - (white_threshold - gradient_range)) / gradient_range
                    alpha_factor = np.clip(alpha_factor, 0, 1)
                    
                    # Make it transparent (1.0 = fully transparent, 0.0 = fully opaque)
                    result[y, x, 3] = 255 * (1.0 - alpha_factor)
                    pixels_modified += 1
    
    print(f"Modified {pixels_modified} pixels for background removal")
    
    # Convert back to uint8
    result = np.clip(result, 0, 255).astype(np.uint8)
    
    # Create high-quality PIL image
    output_img = Image.fromarray(result, mode='RGBA')
    
    # Crop to content (remove fully transparent borders)
    bbox = output_img.getbbox()
    if bbox:
        output_img = output_img.crop(bbox)
        print(f"Cropped to content: {output_img.size}")
    
    # Calculate target size maintaining aspect ratio
    # Target height is 80px for game, calculate width proportionally
    original_width, original_height = output_img.size
    aspect_ratio = original_width / original_height
    
    target_height = 80
    target_width = int(target_height * aspect_ratio)
    
    print(f"Original aspect ratio: {aspect_ratio:.2f}:1")
    print(f"Calculated target size: {target_width}x{target_height}")
    
    # Use LANCZOS (highest quality) for downsampling with antialiasing
    output_img = output_img.resize((target_width, target_height), Image.Resampling.LANCZOS)
    
    # Apply slight sharpening after resize to maintain detail
    from PIL import ImageFilter
    output_img = output_img.filter(ImageFilter.SHARPEN)
    
    # Save as high-quality PNG with maximum compression but no quality loss
    output_img.save(output_path, 'PNG', optimize=True)
    print(f"Final sprite size: {target_width}x{target_height}")
    print(f"Saved HD transparent sprite to: {output_path}")
    
    return output_img

def main():
    input_file = "assets/caveman.jpg"
    output_file = "assets/caveman.png"
    
    print("=" * 70)
    print("HD CAVEMAN SPRITE PROCESSOR - Advanced Background Removal")
    print("=" * 70)
    print(f"Input:  {input_file}")
    print(f"Output: {output_file}")
    print("-" * 70)
    
    try:
        result = remove_background_hd(input_file, output_file)
        print("-" * 70)
        print("✓ SUCCESS! HD transparent sprite created")
        print(f"  Final size: {result.size[0]}x{result.size[1]} pixels")
        print(f"  Quality: Preserved with smooth alpha edges")
        print(f"  Saved to: {output_file}")
        print("=" * 70)
        print("\nNow rebuild the game with: gradlew.bat desktop:dist")
    except FileNotFoundError:
        print(f"✗ ERROR: Could not find {input_file}")
        print("  Please ensure caveman.jpg exists in the assets folder")
    except Exception as e:
        print(f"✗ ERROR: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
