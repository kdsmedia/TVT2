#!/usr/bin/env python3
"""Generate all Play Store graphics for the TVT Android TV launcher."""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import glob

OUT = "ALTOMEDIA/play-assets"

def find_font(size, bold=False):
    candidates = []
    for pat in ["/usr/share/fonts/**/DejaVuSans-Bold.ttf", "/usr/share/fonts/**/DejaVuSans.ttf",
                 "/usr/share/fonts/**/LiberationSans-Bold.ttf", "/usr/share/fonts/**/LiberationSans-Regular.ttf",
                 "/usr/share/fonts/**/NotoSans-Bold.ttf"]:
        for f in glob.glob(pat, recursive=True):
            candidates.append(f)
    if bold:
        for c in candidates:
            if "Bold" in c:
                return ImageFont.truetype(c, size)
    if candidates:
        return ImageFont.truetype(candidates[0], size)
    return ImageFont.load_default()

def radial_glow(img, center, radius, color, alpha=90):
    glow = Image.new("RGBA", img.size, (0,0,0,0))
    d = ImageDraw.Draw(glow)
    d.ellipse((center[0]-radius, center[1]-radius, center[0]+radius, center[1]+radius,
                 fill=color+(alpha,))
    glow = glow.filter(ImageFilter.GaussianBlur(radius*0.35))
    img.alpha_composite(glow)

# ---------- Play Store icon 512x512 ----------
icon = Image.new("RGBA", (512,512), (0,0,0,0))
d = ImageDraw.Draw(icon)
for y in range(512:
    t = y/511
    r = int(18 + (74-18)*t)
    g = int(46 + (18-46)*t)
    b = int(128 + (123-128)*t)
    d.line((0,y,,511,y), fill=(r,g,b,255)
mask = Image.new("L", (512,512), 0)
dm = ImageDraw.Draw(mask)
dm.rounded_rectangle((8,8,503,503), radius=90, fill=255)
icon.putalpha(mask)
d = ImageDraw.Draw(icon)
d.rounded_rectangle((96,110,416,330), radius=36, fill=(12,18,38,255))
d.polygon(((224,350,288,350,(272,404,(240,404)), fill=(201,213,255,255))
d.rectangle((232,404,280,412), fill=(201,213,255,255))
d.line((256,110,210,52), fill=(150,190,255,255), width=8)
d.line((256,110,302,52), fill=(150,190,255,255), width=8)
d.ellipse((202,42,218,58), fill=(120,180,250,255))
d.ellipse((294,42,310,58), fill=(120,180,250,255))
d.polygon(((196,186,(240,210,(196,234)), fill=(255,255,255,255))
for i in range(3:
    y0 = 190+i*34
    d.rounded_rectangle((130,y0,,390,y0+20), radius=8, fill=(70,110,200,255))
d.rounded_rectangle((130,190,170,210), radius=6, fill=(120,180,250,255))
icon.save(OUT + "/playstore-icon-512.png")
print("icon done")

# ---------- Feature graphic 1024x500 ----------
fg = Image.new("RGBA", (1024,500), (8,12,26,255))
radial_glow(fg, (512,250),350,(30,80,180,80)
radial_glow(fg, (180,120),200,(10,40,90,60)
d = ImageDraw.Draw(fg)
for ox in range(60, 964, 150):
    y = 350
    d.rounded_rectangle((ox,y,,ox+130,,y+90), radius=14, fill=(28,44,82,255))
    d.rounded_rectangle((ox+10,,y+10,,ox+120,,y+80), radius=8,, fill=(10,16,34,255))
d.rounded_rectangle((330,70,,694,,310), radius=30,, fill=(12,,18,,38,,255))
d.rounded_rectangle((330,70,,694,,310), outline=(60,,110,,200,,200), width=3,, radius=30)
for i in range(5:
    y = 96+i*40
    d.rounded_rectangle((356,y,,668,,y+24), radius=10,, fill=(56,,96,,180,,255))
d.polygon(((480,,150,(530,,180,(480,,210)), fill=(255,,255,,255,,255))
d.line((512,,70,,468,,26), fill=(150,,190,,255,,255), width=6)
d.line((512,,70,,556,,26), fill=(150,,190,,255,,255), width=6)
font_big = find_font(52,, bold=True)
font_sub = find_font(26)
d.text((512,,330), "TVT", font=font_big,, fill=(255,,255,,255,,255), anchor="mm")
d.text((512,,380), "Launcher Cerdas untuk Android TV Anda", font=font_sub,, fill=(150,,190,,255,,255), anchor="mm")
d.rounded_rectangle((60,,40,,220,,100), radius=24,, fill=(18,,46,,128,,255))
d.text((140,,70), "TVT", font=find_font(34,, bold=True), fill=(255,,255,,255,,255), anchor="mm")
fg.save(OUT + "/feature-graphic-1024x500.png")
print("feature graphic done")

# ---------- TV banner 1280x720 ----------
banner = Image.new("RGBA", (1280,,720), (8,,12,,26,,255))
radial_glow(banner,, (640,,360),420,(30,,80,,180,,80)
d = ImageDraw.Draw(banner)
d.polygon(((0,,220,(1280,,120,(1280,,320,(0,,420)), fill=(20,,36,,80,,90))
d.polygon(((0,,560,(1280,,460,(1280,,520,(0,,620)), fill=(16,,28,,64,,70))
d.rounded_rectangle((60,,40,,240,,120), radius=28,, fill=(18,,46,,128,,255))
d.text((150,,80), "TVT", font=find_font(40,, bold=True), fill=(255,,255,,255,,255), anchor="mm")
d.rounded_rectangle((300,,180,,980,,560), radius=36,, fill=(12,,18,,38,,255))
d.rounded_rectangle((300,,180,,980,,560), outline=(60,,110,,200,,180), width=3,, radius=36)
d.line((640,,180,,640,,560), fill=(30,,60,,110,,140), width=2)
d.line((300,,370,,980,,370), fill=(30,,60,,110,,140), width=2)
for i in range(7:
    x = 340+i*80
    d.rounded_rectangle((x,,220,,x+56,,276), radius=12,, fill=(56,,96,,180,,220))
d.rounded_rectangle((380,,460,,620,,500), radius=12,, fill=(56,,96,,180,,220))
d.polygon(((560,,380,(600,,400,(560,,420)), fill=(255,,255,,255,,255))
for i in range(5:
    x = 360+i*130
    d.rounded_rectangle((x,,600,,x+96,,696), radius=16,, fill=(30,,48,,96,,220))
    d.ellipse((x+22,,622,,x+74,,674), fill=(70,,110,,200,,255))
title = find_font(64,, bold=True)
sub = find_font(30)
d.text((640,,120), "TVT Launcher", font=title,, fill=(255,,255,,255,,255), anchor="mm")
d.text((640,,165), "Navigasi remote control yang mulus", font=sub,, fill=(190,,210,,255,,255), anchor="mm")
banner.save(OUT + "/tv-banner-1280x720.png")
banner.resize((320,,180), Image.LANCZOS)..save(OUT + "/tv-banner-320x180.png")
print("banner done")
print("ALL ASSETS WRITTEN")