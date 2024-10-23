#version 330 core

#define BILLBOARD_MODE_NONE 0
#define BILLBOARD_MODE_SPHERICAL 1
#define BILLBOARD_MODE_CYLINDRICAL 2

mat4 billboard(mat4 pModel, mat4 pView, int mode) {

  vec3 scaling = vec3(1);
  scaling.x = length(pModel[0].xyz);
  scaling.y = length(pModel[1].xyz);
  scaling.z = length(pModel[2].xyz);

  mat4 modelView = mat4(pView * pModel);
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
