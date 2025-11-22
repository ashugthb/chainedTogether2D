"""
Create high-quality PNG textures for the game
Generates platform, ice, ramp, and improved player sprites
"""
from PIL import Image, ImageDraw
import os

# Output directory
ASSETS_DIR = "assets"
os.makedirs(ASSETS_DIR, exist_ok=True)

def create_enhanced_caveman(size=128):
    """Create a larger, more detailed caveman sprite"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Scale factors
    cx, cy = size // 2, size // 2
    
    # Body - brown fur vest (larger)
    body_color = (101, 67, 33)
    draw.ellipse([cx-24, cy-8, cx+24, cy+40], fill=body_color)
    
    # Arms
    arm_color = (210, 180, 140)
    # Left arm
    draw.ellipse([cx-32, cy, cx-16, cy+32], fill=arm_color)
    # Right arm
    draw.ellipse([cx+16, cy, cx+32, cy+32], fill=arm_color)
    
    # Legs
    leg_color = (210, 180, 140)
    # Left leg
    draw.rectangle([cx-16, cy+36, cx-4, cy+60], fill=leg_color)
    # Right leg
    draw.rectangle([cx+4, cy+36, cx+16, cy+60], fill=leg_color)
    
    # Feet (brown)
    foot_color = (101, 67, 33)
    draw.ellipse([cx-18, cy+56, cx-2, cy+64], fill=foot_color)
    draw.ellipse([cx+2, cy+56, cx+18, cy+64], fill=foot_color)
    
    # Head - tan skin
    skin_color = (210, 180, 140)
    draw.ellipse([cx-20, cy-40, cx+20, cy], fill=skin_color)
    
    # Hair - dark brown messy hair
    hair_color = (70, 40, 20)
    draw.ellipse([cx-22, cy-42, cx+22, cy-20], fill=hair_color)
    # Hair tufts
    draw.ellipse([cx-24, cy-38, cx-12, cy-28], fill=hair_color)
    draw.ellipse([cx+12, cy-38, cx+24, cy-28], fill=hair_color)
    
    # Face features
    # Eyes
    eye_color = (50, 30, 20)
    draw.ellipse([cx-12, cy-26, cx-6, cy-20], fill=eye_color)
    draw.ellipse([cx+6, cy-26, cx+12, cy-20], fill=eye_color)
    
    # Smile
    draw.arc([cx-10, cy-18, cx+10, cy-8], 0, 180, fill=(50, 30, 20), width=2)
    
    # Add outline for better visibility
    draw.ellipse([cx-20, cy-40, cx+20, cy], outline=(0, 0, 0), width=2)
    
    return img

def create_platform_texture(width=200, height=30):
    """Create a stone platform texture"""
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Base gray color
    base_color = (136, 136, 136)
    draw.rectangle([0, 0, width, height], fill=base_color)
    
    # Add stone texture with random darker/lighter spots
    import random
    random.seed(42)  # Consistent pattern
    for i in range(50):
        x = random.randint(0, width-10)
        y = random.randint(0, height-5)
        size = random.randint(3, 8)
        shade = random.randint(-30, 30)
        color = (
            max(0, min(255, base_color[0] + shade)),
            max(0, min(255, base_color[1] + shade)),
            max(0, min(255, base_color[2] + shade))
        )
        draw.ellipse([x, y, x+size, y+size], fill=color)
    
    # Border
    draw.rectangle([0, 0, width-1, height-1], outline=(85, 85, 85), width=2)
    
    # Top highlight
    draw.line([(2, 2), (width-3, 2)], fill=(180, 180, 180), width=2)
    
    return img

def create_ice_texture(width=200, height=30):
    """Create an icy blue platform texture"""
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Ice blue color with transparency
    ice_color = (173, 216, 230, 240)
    draw.rectangle([0, 0, width, height], fill=ice_color)
    
    # Add crystalline effect
    import random
    random.seed(123)
    for i in range(30):
        x = random.randint(0, width)
        y = random.randint(0, height)
        size = random.randint(2, 6)
        # Lighter blue spots
        draw.ellipse([x, y, x+size, y+size], fill=(200, 230, 255, 200))
    
    # Border - darker blue
    draw.rectangle([0, 0, width-1, height-1], outline=(100, 150, 200), width=2)
    
    # Shine effect on top
    draw.line([(0, 1), (width, 1)], fill=(220, 240, 255), width=2)
    
    return img

def create_ramp_texture(width=200, height=200, facing_right=True):
    """Create a stone ramp texture"""
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Tan/brown color for ramps
    ramp_color = (179, 153, 102)
    
    if facing_right:
        # Triangle pointing up-right: /
        points = [(0, height), (width, height), (width, 0)]
    else:
        # Triangle pointing up-left: \
        points = [(0, height), (width, height), (0, 0)]
    
    draw.polygon(points, fill=ramp_color)
    
    # Add texture
    import random
    random.seed(456)
    for i in range(40):
        if facing_right:
            x = random.randint(width//2, width)
            y = random.randint(0, height)
        else:
            x = random.randint(0, width//2)
            y = random.randint(0, height)
        size = random.randint(2, 6)
        shade = random.randint(-20, 20)
        color = (
            max(0, min(255, ramp_color[0] + shade)),
            max(0, min(255, ramp_color[1] + shade)),
            max(0, min(255, ramp_color[2] + shade))
        )
        draw.ellipse([x, y, x+size, y+size], fill=color)
    
    # Border
    draw.polygon(points, outline=(128, 109, 73), width=3)
    
    return img

def create_spike_texture(width=30, height=30):
    """Create a dangerous spike texture"""
    img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Red spike
    spike_color = (200, 0, 0)
    points = [(width//2, 0), (width, height), (0, height)]
    draw.polygon(points, fill=spike_color)
    
    # Darker outline
    draw.polygon(points, outline=(120, 0, 0), width=2)
    
    # Highlight on edge
    draw.line([(width//2, 0), (0, height)], fill=(255, 100, 100), width=2)
    
    return img

# Generate all textures
print("Generating game textures...")

# Enhanced caveman (bigger)
caveman = create_enhanced_caveman(128)
caveman.save(os.path.join(ASSETS_DIR, "caveman.png"))
print("✓ Created caveman.png (128x128)")

# Platform texture
platform = create_platform_texture(200, 30)
platform.save(os.path.join(ASSETS_DIR, "platform.png"))
print("✓ Created platform.png (200x30)")

# Ice texture
ice = create_ice_texture(200, 30)
ice.save(os.path.join(ASSETS_DIR, "ice.png"))
print("✓ Created ice.png (200x30)")

# Ramp textures
ramp_right = create_ramp_texture(200, 200, facing_right=True)
ramp_right.save(os.path.join(ASSETS_DIR, "ramp_right.png"))
print("✓ Created ramp_right.png (200x200)")

ramp_left = create_ramp_texture(200, 200, facing_right=False)
ramp_left.save(os.path.join(ASSETS_DIR, "ramp_left.png"))
print("✓ Created ramp_left.png (200x200)")

# Spike texture
spike = create_spike_texture(30, 30)
spike.save(os.path.join(ASSETS_DIR, "spike.png"))
print("✓ Created spike.png (30x30)")

print("\n✓ All textures generated successfully!")
print(f"Textures saved to: {os.path.abspath(ASSETS_DIR)}")
