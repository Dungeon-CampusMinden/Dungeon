interface Position {
  x: number;
  y: number;
}

export class Character {
  private position: Position | undefined;

  constructor() {}

  setPosition(position: string) {
    this.position = position.split(",").reduce(
      (acc, val, i) => {
        if (i === 0) {
          acc.x = Number(parseFloat(val).toFixed(2));
        } else {
          acc.y = Number(parseFloat(val).toFixed(2));
        }
        return acc;
      },
      { x: 0, y: 0 }
    );
  }

  getPosition() {
    return this.position;
  }
}
