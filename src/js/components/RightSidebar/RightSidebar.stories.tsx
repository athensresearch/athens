import { RightSidebar } from './RightSidebar';
import { AppLayout } from '../AppLayout';

export default {
  title: 'Sections/RightSidebar',
  component: RightSidebar,
  argTypes: {},
};

const Template = (args) => <AppLayout style={{ minHeight: "60vh" }}><RightSidebar {...args} /></AppLayout>;

export const Primary = Template.bind({});
Primary.args = {
  isRightSidebarOpen: true,
  items: [{
    title: 'Item 1',
    isOpen: true,
    type: 'block',
    body: <span>Lorem ipsum dolor sit amet</span>,
    handlePressItemToggle: () => null,
    handlePressItemClose: () => null,
  }, {
    title: 'Item 2',
    isOpen: true,
    type: 'block',
    body: <span>Lorem ipsum dolor sit amet</span>,
    handlePressItemToggle: () => null,
    handlePressItemClose: () => null,
  }]
};

export const Empty = Template.bind({});
Empty.args = {
  isRightSidebarOpen: true,
};

