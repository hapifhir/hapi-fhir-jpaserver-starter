import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperationResultComponent } from './operation-result.component';

describe('OperationOutcomeComponent', () => {
  let component: OperationResultComponent;
  let fixture: ComponentFixture<OperationResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OperationResultComponent],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OperationResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
