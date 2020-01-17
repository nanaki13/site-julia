import { Id } from "../Id";
import { Updatable } from "../Updatable";
import { Image } from "../Image";
import { MenuItem } from '../MenuItem';
import { Oeuvre } from "./Oeuvre";
export interface PageElement extends Id, Updatable {
  x: number;
  y: number;
  alt: string;
  title: string;
  type: string;
  themeKey: number;
  image: Image;
  src: string;
  isNonEmpty(): boolean;
  isOeuvre(): boolean;
  isMenu(): boolean;
  asOeuvre(): Oeuvre;
  asMenu(): MenuItem;
}
