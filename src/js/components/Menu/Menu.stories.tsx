import { BADGE, Storybook } from '../../storybook';

import { Link } from '@material-ui/icons'

import { Menu } from './Menu';
import { Overlay } from '../Overlay';

export default {
  title: 'Components/Menu',
  component: Menu,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <Menu {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  children: <>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Separator />
    <Menu.Button>Menu Item</Menu.Button>
  </>,
};

export const InAnOverlay = Template.bind({});
InAnOverlay.args = {
  children: <>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Button>Menu Item</Menu.Button>
    <Menu.Separator />
    <Menu.Button>Menu Item</Menu.Button>
  </>,
};
InAnOverlay.decorators = [(Story) => <Storybook.Wrapper><Overlay><Story /></Overlay></Storybook.Wrapper>]

export const Typical = Template.bind({});
Typical.args = {
  children: <>
    <Menu.Button><Link />Menu Item</Menu.Button>
    <Menu.Button><Link />Menu Item</Menu.Button>
    <Menu.Button><Link />Menu Item</Menu.Button>
    <Menu.Separator />
    <Menu.Button>Menu Item</Menu.Button>
  </>
};
Typical.decorators = [(Story) => <Storybook.Wrapper><Overlay><Story /></Overlay></Storybook.Wrapper>]
