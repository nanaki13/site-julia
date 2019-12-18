/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { OeuvreService } from './oeuvre.service';

describe('Service: OeuvreService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OeuvreService]
    });
  });

  it('should ...', inject([OeuvreService], (service: OeuvreService) => {
    expect(service).toBeTruthy();
  }));
});
