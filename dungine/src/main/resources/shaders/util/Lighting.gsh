vec3 normal(vec3 v1, vec3 v2, vec3 v3) {
  return normalize(cross(v2 - v1, v3 - v1));
}

vec3 tangent(vec3 v1, vec3 v2, vec3 v3) {
    return normalize(v2 - v1);
}

vec3 bitangent(vec3 v1, vec3 v2, vec3 v3) {
    vec3 normal = normal(v1, v2, v3);
    vec3 tangent = tangent(v1, v2, v3);
    return normalize(cross(normal, tangent));
}
