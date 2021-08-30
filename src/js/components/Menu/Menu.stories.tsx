import styled from 'styled-components';

import { Menu } from './Menu';
import { Button } from '../Button';
import { Overlay } from '../Overlay';

const Wrapper = styled.div`
  padding: 4rem;
  margin: auto;
`;

export default {
  title: 'Components/Menu',
  component: Menu,
  argTypes: {},
  parameters: {
    layout: 'centered'
  }
};

const Template = (args) => <Wrapper><Menu {...args} /></Wrapper>;

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
