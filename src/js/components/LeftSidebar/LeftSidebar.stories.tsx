import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';

import { LeftSidebar } from './LeftSidebar';
import { AppLayout } from '../AppLayout';

export default {
  title: 'Sections/LeftSidebar',
  component: LeftSidebar,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  }
};

const Template = (args) => <AppLayout style={{ minHeight: "60vh" }}>
  <LeftSidebar {...args} />
</AppLayout>;

export const Basic = Template.bind({});
Basic.args = {
  isLeftSidebarOpen: true,
  shortcuts: [{
    uid: "4b89dde0-3ccf-481a-875b-d11adfda3f7e",
    title: "Passer domesticus",
    order: 1
  },],
  version: '1.0.0'
};

export const ManyShortcuts = Template.bind({});
ManyShortcuts.args = {
  isLeftSidebarOpen: true,
  shortcuts: [
    {
      uid: "4b89dde0-3ccf-481a-875b-d11adfda3f7e",
      title: "Passer domesticus",
      order: 1
    }, {
      uid: "bd4a892f-c7e5-45d8-bab8-68a8ed9d224f",
      title: "Spermophilus richardsonii",
      order: 2
    }, {
      uid: "b60fc12e-bf48-415c-a059-a7a4d5ef686e",
      title: "Leprocaulinus vipera",
      order: 3
    }, {
      uid: "c58d62e5-0e1b-4f30-a156-af8467317c1c",
      title: "Rangifer tarandus",
      order: 4
    }, {
      uid: "dd099e5d-1f6d-4be7-8bf0-9fc0310ba489",
      title: "Nycticorax nycticorax",
      order: 5
    }, {
      uid: "819f946d-9fbe-43e4-bd6c-6d9a6577d06e",
      title: "Hystrix cristata",
      order: 6
    }, {
      uid: "1ad4b569-252a-4778-a267-6d3d53bd3fb7",
      title: "Paraxerus cepapi",
      order: 7
    }, {
      uid: "ac370365-498c-4730-94d7-076756407062",
      title: "Neophron percnopterus",
      order: 8
    }, {
      uid: "f3471fcc-abc2-4ad5-bdc6-85d60d6b482e",
      title: "Nyctanassa violacea",
      order: 9
    }, {
      uid: "c527bd52-f128-4395-8d30-667ec360d2f7",
      title: "Dasypus septemcincus",
      order: 10
    }, {
      uid: "5fe97b52-3e11-4a08-b315-40974fa78b8b",
      title: "Dusicyon thous",
      order: 11
    }, {
      uid: "bc9a9677-50f6-428b-aa2c-964fca8319fe",
      title: "Macropus robustus",
      order: 12
    }, {
      uid: "1df27ac7-850e-48a5-b350-5fee2464d326",
      title: "Macropus agilis",
      order: 13
    }, {
      uid: "3023ecbf-bd11-4985-a66b-e3e7a18d1a71",
      title: "Spermophilus armatus",
      order: 14
    }, {
      uid: "3af40f86-c773-436f-9de6-b1c1fda94972",
      title: "Podargus strigoides",
      order: 15
    }, {
      uid: "e4f8385a-65e6-4d06-985c-e6fc14386b97",
      title: "Laniaurius atrococcineus",
      order: 16
    }, {
      uid: "f0d64494-71ff-403d-8edf-3b21ca77e782",
      title: "Geochelone elegans",
      order: 17
    }, {
      uid: "c1565410-6d71-4984-8456-5e3e90751b7a",
      title: "Pteronura brasiliensis",
      order: 18
    }, {
      uid: "506d0bc7-23f2-4b31-a0cd-64cdbcd3b5dc",
      title: "Hystrix indica",
      order: 19
    }, {
      uid: "95b0e93f-bf81-46bc-a667-57ca1b6bdac8",
      title: "Theropithecus gelada",
      order: 20
    }, {
      uid: "67b65377-6129-4240-92bc-d566ac471cee",
      title: "Libellula quadrimaculata",
      order: 21
    }, {
      uid: "69992624-1211-4514-a6b6-121f4a2bab9e",
      title: "Procyon lotor",
      order: 22
    }, {
      uid: "6f7b4a22-62ff-4af2-b635-2b79dc48e290",
      title: "Numida meleagris",
      order: 23
    }, {
      uid: "f3e79018-39ce-4d21-979a-468706501fa8",
      title: "Pycnonotus nigricans",
      order: 24
    }, {
      uid: "36b6e034-87e9-4b91-87e7-4663d8792d37",
      title: "Macropus parryi",
      order: 25
    }
  ],
  version: '1.0.0'
};

