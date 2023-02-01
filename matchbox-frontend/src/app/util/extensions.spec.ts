/// <reference path=".,/../../../fhir.r4/index.d.ts" />

import { TestBed, getTestBed } from '@angular/core/testing';

describe('Utilities', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  describe('#Patient with extension test, see https://www.hl7.org/fhir/patient-example-dicom.json.html', () => {
    it('extensions should be accessible with type defintions for fhir', () => {
      const patient: fhir.r4.Patient = {
        resourceType: 'Patient',
        id: 'dicom',
        text: {
          status: 'generated',
          // tslint:disable-next-line:max-line-length
          div:
            '<div xmlns="http://www.w3.org/1999/xhtml"> Patient MINT_TEST, ID = MINT1234. Age = 56y, Size =\n      1.83m, Weight = 72.58kg </div>',
        },
        extension: [
          {
            url: 'http://nema.org/fhir/extensions#0010:1010',
            valueQuantity: {
              value: 56,
              unit: 'Y',
            },
          },
          {
            url: 'http://nema.org/fhir/extensions#0010:1020',
            valueQuantity: {
              value: 1.83,
              unit: 'm',
            },
          },
          {
            url: 'http://nema.org/fhir/extensions#0010:1030',
            valueQuantity: {
              value: 72.58,
              unit: 'kg',
            },
          },
        ],
        identifier: [
          {
            system: 'http://nema.org/examples/patients',
            value: 'MINT1234',
          },
        ],
        active: true,
        name: [
          {
            family: 'MINT_TEST',
          },
        ],
        gender: 'male',
        _gender: {
          extension: [
            {
              url: 'http://nema.org/examples/extensions#gender',
              valueCoding: {
                system: 'http://nema.org/examples/gender',
                code: 'M',
              },
            },
          ],
        },
        managingOrganization: {
          reference: 'Organization/1',
        },
      };

      expect(patient.extension[0].valueQuantity.value).toBe(56);
      expect(patient._gender.extension[0].valueCoding.code).toBe('M');

      patient.extension[0].valueQuantity.value = 12;
      expect(patient.extension[0].valueQuantity.value).toBe(12);
    });
  });
});
