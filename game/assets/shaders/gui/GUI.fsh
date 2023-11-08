#version 330 core

#define PROP_NONE 0x0000
#define PROP_HAS_BACKGROUND_COLOR 0x0001
#define PROP_HAS_BACKGROUND_IMAGE 0x0002
#define PROP_HAS_TOP_BORDER 0x0004
#define PROP_HAS_RIGHT_BORDER 0x0008
#define PROP_HAS_BOTTOM_BORDER 0x0010
#define PROP_HAS_LEFT_BORDER 0x0020

in vec2 vTexCoord;
in vec3 vElementPos;
in vec3 vFragmentPos;

uniform int uProperties;
uniform vec4 uBackgroundColor;
uniform vec4 uBorderColor;

uniform sampler2D uBackgroundTexture;

layout(location = 0) out vec4 fragColor;

void main() {

    if((uProperties & PROP_HAS_BACKGROUND_COLOR) == PROP_HAS_BACKGROUND_COLOR
        && (uProperties & PROP_HAS_BACKGROUND_IMAGE) != PROP_HAS_BACKGROUND_IMAGE) { //Background color, no Background image
        fragColor = uBackgroundColor;
    } else if((uProperties & PROP_HAS_BACKGROUND_IMAGE) == PROP_HAS_BACKGROUND_IMAGE
            && (uProperties & PROP_HAS_BACKGROUND_COLOR) != PROP_HAS_BACKGROUND_COLOR) { //Background image, no Background color
        fragColor = texture(uBackgroundTexture, vTexCoord);
    } else if((uProperties & PROP_HAS_BACKGROUND_IMAGE) == PROP_HAS_BACKGROUND_IMAGE
            && (uProperties & PROP_HAS_BACKGROUND_COLOR) == PROP_HAS_BACKGROUND_COLOR) { //Background image and color -> tinting
        fragColor = texture(uBackgroundTexture, vTexCoord) * uBackgroundColor;
    } else { //No background image or color
        fragColor = vec4(vFragmentPos.x, 0.0f, vFragmentPos.y, 0.8f);
    }

    //TODO: Borders

}
