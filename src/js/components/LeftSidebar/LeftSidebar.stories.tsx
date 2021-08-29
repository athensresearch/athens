import styled from 'styled-components';

import { LeftSidebar } from './LeftSidebar';

export default {
  title: 'Components/LeftSidebar',
  component: LeftSidebar,
  argTypes: {},
};

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  height: 100vh;
`;

const Template = (args) => <Wrapper><LeftSidebar {...args} /></Wrapper>;

export const Primary = Template.bind({});
Primary.args = {
  isLeftSidebarOpen: true,
  shortcuts: [{ uid: '1', title: 'James and the giant', order: 0 }],
  version: '1.0.0'
};
Primary.properties = {
  layout: 'fullscreen'
}

