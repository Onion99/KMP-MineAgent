#!/usr/bin/env python3
"""Generate desktop package icons from the Android launcher vector."""

from __future__ import annotations

import base64
import os
import struct
import subprocess
import tempfile
import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
#ANDROID_VECTOR = ROOT / "composeApp/src/androidMain/res/drawable/ic_launcher_round.xml"
ANDROID_VECTOR = ROOT / "composeApp/src/androidMain/res/drawable/ic_launcher.xml"
OUTPUT_DIR = ROOT / "docs"
SVG_OUTPUT = OUTPUT_DIR / "AppIcon.svg"
PNG_OUTPUT = OUTPUT_DIR / "AppIcon.png"
ICO_OUTPUT = OUTPUT_DIR / "AppIcon.ico"
ICNS_OUTPUT = OUTPUT_DIR / "AppIcon.icns"

ANDROID_NS = "{http://schemas.android.com/apk/res/android}"
AAPT_NS = "{http://schemas.android.com/aapt}"
RENDER_SIZE = 1024


def android_attr(name: str) -> str:
    return f"{ANDROID_NS}{name}"


def parse_android_color(value: str) -> tuple[str, float]:
    color = value.strip()
    if len(color) == 9 and color.startswith("#"):
        alpha = int(color[1:3], 16) / 255
        rgb = f"#{color[3:]}"
        return rgb, alpha
    if len(color) == 7 and color.startswith("#"):
        return color, 1.0
    return color, 1.0


def float_attr(element: ET.Element, name: str, default: float) -> float:
    value = element.get(android_attr(name))
    return default if value is None else float(value)


def find_chrome() -> Path:
    candidates = [
        os.environ.get("CHROME"),
        r"C:\Program Files\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
        r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
        r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    ]
    for candidate in candidates:
        if candidate and Path(candidate).exists():
            return Path(candidate)
    raise RuntimeError("Chrome or Edge is required to rasterize SVG icons.")


def vector_to_svg(vector_path: Path, svg_path: Path) -> None:
    tree = ET.parse(vector_path)
    root = tree.getroot()
    viewport_width = root.get(android_attr("viewportWidth"), "512")
    viewport_height = root.get(android_attr("viewportHeight"), "512")

    gradient_defs: list[str] = []
    path_nodes: list[str] = []

    for index, path in enumerate(root.findall("path")):
        path_data = path.get(android_attr("pathData"))
        if not path_data:
            continue

        attributes: list[str] = [f'd="{path_data}"']
        fill_alpha = float_attr(path, "fillAlpha", 1.0)
        stroke_alpha = float_attr(path, "strokeAlpha", 1.0)

        gradient = path.find(f"{AAPT_NS}attr/gradient")
        if gradient is not None:
            gradient_id = f"gradient{index}"
            x1 = gradient.get(android_attr("startX"), "0")
            y1 = gradient.get(android_attr("startY"), "0")
            x2 = gradient.get(android_attr("endX"), "0")
            y2 = gradient.get(android_attr("endY"), "0")
            stops: list[str] = []
            for item in gradient.findall("item"):
                offset = item.get(android_attr("offset"), "0")
                color, alpha = parse_android_color(item.get(android_attr("color"), "#000000"))
                stops.append(
                    f'<stop offset="{float(offset) * 100:g}%" stop-color="{color}" '
                    f'stop-opacity="{alpha:g}" />'
                )
            gradient_defs.append(
                f'<linearGradient id="{gradient_id}" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" '
                f'gradientUnits="userSpaceOnUse">{"".join(stops)}</linearGradient>'
            )
            attributes.append(f'fill="url(#{gradient_id})"')
        else:
            fill_color = path.get(android_attr("fillColor"))
            if fill_color:
                color, alpha = parse_android_color(fill_color)
                fill_alpha *= alpha
                attributes.append(f'fill="{color}"')
            else:
                attributes.append('fill="none"')

        attributes.append(f'fill-opacity="{fill_alpha:g}"')

        stroke_color = path.get(android_attr("strokeColor"))
        if stroke_color:
            color, alpha = parse_android_color(stroke_color)
            attributes.append(f'stroke="{color}"')
            attributes.append(f'stroke-opacity="{stroke_alpha * alpha:g}"')
            attributes.append(f'stroke-width="{path.get(android_attr("strokeWidth"), "1")}"')
            line_cap = path.get(android_attr("strokeLineCap"))
            if line_cap:
                attributes.append(f'stroke-linecap="{line_cap}"')

        path_nodes.append(f'<path {" ".join(attributes)} />')

    svg = (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{RENDER_SIZE}" height="{RENDER_SIZE}" '
        f'viewBox="0 0 {viewport_width} {viewport_height}">'
        f'<defs>{"".join(gradient_defs)}</defs>'
        f'{"".join(path_nodes)}</svg>\n'
    )
    svg_path.write_text(svg, encoding="utf-8")


def render_svg_to_png(svg_path: Path, png_path: Path) -> None:
    chrome = find_chrome()
    html = (
        "<!doctype html><html><head><style>"
        "html,body{margin:0;width:100%;height:100%;background:transparent;overflow:hidden}"
        "img{display:block;width:100vw;height:100vh}"
        "</style></head><body>"
        f'<img src="data:image/svg+xml;base64,{base64.b64encode(svg_path.read_bytes()).decode("ascii")}">'
        "</body></html>"
    )
    with tempfile.NamedTemporaryFile("w", suffix=".html", delete=False, encoding="utf-8") as file:
        file.write(html)
        html_path = Path(file.name)
    try:
        subprocess.run(
            [
                str(chrome),
                "--headless=new",
                "--disable-gpu",
                "--hide-scrollbars",
                f"--window-size={RENDER_SIZE},{RENDER_SIZE}",
                f"--screenshot={png_path}",
                html_path.as_uri(),
            ],
            check=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    finally:
        html_path.unlink(missing_ok=True)

    with Image.open(png_path) as image:
        if image.size != (RENDER_SIZE, RENDER_SIZE):
            raise RuntimeError(f"Unexpected rendered PNG size: {image.size}")


def save_png_icon(source_png: Path, target_png: Path) -> None:
    with Image.open(source_png) as image:
        image.resize((512, 512), Image.Resampling.LANCZOS).save(target_png, "PNG")


def save_ico(source_png: Path, target_ico: Path) -> None:
    sizes = [(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    with Image.open(source_png) as image:
        image.save(target_ico, format="ICO", sizes=sizes)


def save_icns(source_png: Path, target_icns: Path) -> None:
    sizes = [16, 32, 64, 128, 256, 512, 1024]
    with Image.open(source_png) as image:
        image.save(target_icns, format="ICNS", sizes=sizes)

    # Validate the container header so Gradle/jpackage does not receive a PNG with a wrong extension.
    data = target_icns.read_bytes()
    if data[:4] != b"icns" or struct.unpack(">I", data[4:8])[0] != len(data):
        raise RuntimeError("Generated ICNS file has an invalid header.")


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    vector_to_svg(ANDROID_VECTOR, SVG_OUTPUT)

    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as file:
        rendered_png = Path(file.name)
    try:
        render_svg_to_png(SVG_OUTPUT, rendered_png)
        save_png_icon(rendered_png, PNG_OUTPUT)
        save_ico(rendered_png, ICO_OUTPUT)
        save_icns(rendered_png, ICNS_OUTPUT)
    finally:
        rendered_png.unlink(missing_ok=True)

    for path in [SVG_OUTPUT, PNG_OUTPUT, ICO_OUTPUT, ICNS_OUTPUT]:
        print(f"{path.relative_to(ROOT)} ({path.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
