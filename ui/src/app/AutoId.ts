export class AutoId {
  id = 0;
  getId() {
    this.id++;
    return this.id;
  }
}
