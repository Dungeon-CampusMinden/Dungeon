import type { DeerSchema, Objective, Surface } from "./DeerSchema";

export class Util {
  static generateUniqueId(prefix: string = "id"): string {
    const randomString = Math.random().toString(36).substring(2, 10);
    return `${prefix}_${randomString}`;
  }

  static generateSafeInteger(): number {
    return Math.floor(Math.random() * (Number.MAX_SAFE_INTEGER + 1));
  }

  static getLearningObjective(deerSchema: DeerSchema, objectiveId: string): Objective | undefined {
    return deerSchema.learningDesign.objectives.find((objective) => objective.id === objectiveId);
  }

  static getSurface(deerSchema: DeerSchema, surfaceId: string): Surface | undefined {
    return deerSchema.surfaces.find((surface) => surface.id === surfaceId);
  }
}
