from PIL import Image, ImageDraw

# Create a 64x64 pixel caveman sprite
width, height = 64, 64
img = Image.new('RGBA', (width, height), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Skin color (tan/peach)
skin_color = (210, 160, 120, 255)
# Brown for hair/fur
brown_color = (101, 67, 33, 255)
# Dark brown for outlines
dark_brown = (60, 40, 20, 255)
# White for eyes
white = (255, 255, 255, 255)
# Black for pupils
black = (0, 0, 0, 255)

# Body proportions for a caveman character
# Head (12 pixels from top, 20x20)
head_x = 22
head_y = 12
head_size = 20

# Draw messy hair on top and sides
draw.ellipse([head_x-4, head_y-6, head_x+head_size+4, head_y+8], fill=brown_color)

# Draw head (circle)
draw.ellipse([head_x, head_y, head_x+head_size, head_y+head_size], fill=skin_color)
# Head outline
draw.ellipse([head_x, head_y, head_x+head_size, head_y+head_size], outline=dark_brown, width=2)

# Draw eyes
eye_y = head_y + 8
draw.ellipse([head_x+5, eye_y, head_x+9, eye_y+4], fill=white)
draw.ellipse([head_x+head_size-9, eye_y, head_x+head_size-5, eye_y+4], fill=white)
# Pupils
draw.ellipse([head_x+6, eye_y+1, head_x+8, eye_y+3], fill=black)
draw.ellipse([head_x+head_size-8, eye_y+1, head_x+head_size-6, eye_y+3], fill=black)

# Draw simple smile
draw.arc([head_x+6, head_y+10, head_x+head_size-6, head_y+18], 0, 180, fill=dark_brown, width=2)

# Body (torso) - wearing animal fur
body_y = head_y + head_size - 2
body_width = 24
body_height = 18
body_x = (width - body_width) // 2
# Fur vest (brown rectangle with jagged bottom)
draw.rectangle([body_x, body_y, body_x+body_width, body_y+body_height], fill=brown_color, outline=dark_brown, width=2)

# Arms (thin rectangles)
arm_width = 6
arm_length = 20
# Left arm
draw.rectangle([body_x-4, body_y+2, body_x+2, body_y+2+arm_length], fill=skin_color, outline=dark_brown, width=1)
# Right arm
draw.rectangle([body_x+body_width-2, body_y+2, body_x+body_width+4, body_y+2+arm_length], fill=skin_color, outline=dark_brown, width=1)

# Legs (two thin rectangles)
leg_width = 8
leg_length = 16
leg_y = body_y + body_height - 2
# Left leg
draw.rectangle([body_x+4, leg_y, body_x+4+leg_width, leg_y+leg_length], fill=brown_color, outline=dark_brown, width=2)
# Right leg
draw.rectangle([body_x+body_width-leg_width-4, leg_y, body_x+body_width-4, leg_y+leg_length], fill=brown_color, outline=dark_brown, width=2)

# Feet (brown ovals)
foot_width = 10
foot_height = 6
feet_y = leg_y + leg_length - 4
draw.ellipse([body_x+2, feet_y, body_x+2+foot_width, feet_y+foot_height], fill=brown_color, outline=dark_brown, width=1)
draw.ellipse([body_x+body_width-foot_width-2, feet_y, body_x+body_width-2, feet_y+foot_height], fill=brown_color, outline=dark_brown, width=1)

# Save the sprite
img.save('assets/caveman.png')
print("Caveman sprite created at assets/caveman.png")
