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
    }
};

const Template = (args) => <Storybook.Wrapper><Menu {...args} /></Storybook.Wrapper>;

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
    children:
        <Overlay>
            <Menu.Button>Menu Item</Menu.Button>
            <Menu.Button>Menu Item</Menu.Button>
            <Menu.Button>Menu Item</Menu.Button>
            <Menu.Separator />
            <Menu.Button>Menu Item</Menu.Button>
        </Overlay>,
};

export const Typical = Template.bind({});
Typical.args = {
    children:
        <Overlay>
            <Menu.Button><Link />Menu Item</Menu.Button>
            <Menu.Button><Link />Menu Item</Menu.Button>
            <Menu.Button><Link />Menu Item</Menu.Button>
            <Menu.Separator />
            <Menu.Button>Menu Item</Menu.Button>
        </Overlay>,
};
