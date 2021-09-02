import { BADGE, Storybook } from '../../storybook';

import { Menu } from './Menu';
import { Button } from '../Button';
import { Overlay } from '../Overlay';

export default {
  title: 'Components/Menu',
  component: Menu,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  }
};

const Template = (args) => <Storybook.Wrapper><Menu {...args} /></Storybook.Wrapper>;

export const Basic = Template.bind({});
Basic.args = {
  children: <>
    <Button>Button</Button>
    <Button>Button</Button>
    <Button>Button</Button>
    <Menu.Separator />
    <Button>Button</Button>
  </>,
};

export const InAnOverlay = Template.bind({});
InAnOverlay.args = {
  children:
    <Overlay>
      <Button>Button</Button>
      <Button>Button</Button>
      <Button>Button</Button>
      <Menu.Separator />
      <Button>Button</Button>
    </Overlay>,
};
