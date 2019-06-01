/* tslint:disable:no-unused-variable */

import { TestBed, async, inject } from '@angular/core/testing';
import { MessageInternService } from './message-intern.service';

describe('Service: MessageIntern', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MessageInternService]
    });
  });

  it('should ...', inject([MessageInternService], (service: MessageInternService) => {
    expect(service).toBeTruthy();
  }));
});
