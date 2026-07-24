export class Util {
  static generateUniqueId(prefix: string = "id"): string {
    const randomString = Math.random().toString(36).substring(2, 10);
    return `${prefix}_${randomString}`;
  }
}
