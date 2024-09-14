#version 330 core

#define BILLBOARD_MODE_NONE 0
#define BILLBOARD_MODE_SPHERICAL 1
#define BILLBOARD_MODE_CYLINDRICAL 2

mat4 billboard(mat4 pModelView, vec3 scaling, int mode) {
  mat4 modelView = mat4(pModelView);
  switch (mode) {
    case BILLBOARD_MODE_SPHERICAL:
          modelView[0][0] = scaling.x;
          modelView[0][1] = 0.0;
          modelView[0][2] = 0.0;
          modelView[1][0] = 0.0;
          modelView[1][1] = scaling.y;
          modelView[1][2] = 0.0;
          modelView[2][0] = 0.0;
          modelView[2][1] = 0.0;
          modelView[2][2] = scaling.z;
          break;
    case BILLBOARD_MODE_CYLINDRICAL:
          modelView[0][0] = scaling.x;
          modelView[0][1] = 0.0;
          modelView[0][2] = 0.0;
          modelView[2][0] = 0.0;
          modelView[2][1] = 0.0;
          modelView[2][2] = scaling.z;
          break;
    default:
          break;
  }
  return modelView;
}
