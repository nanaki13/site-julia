import { Updatable } from "./Updatable";
export class Image implements Updatable {
  id: number;
  link: string;
  name: string;
  updateStatus: boolean;
  updated: boolean;
}
