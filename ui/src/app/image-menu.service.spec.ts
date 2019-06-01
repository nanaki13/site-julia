/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { ImageMenuService } from './image-menu.service';

describe('Service: ImageMenu', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ImageMenuService]
    });
  });

  it('should ...', inject([ImageMenuService], (service: ImageMenuService) => {
    expect(service).toBeTruthy();
  }));
});
