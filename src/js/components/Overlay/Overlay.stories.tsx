import styled from 'styled-components';
import { Create } from '@material-ui/icons';

import { Overlay } from './Overlay';
import { Menu } from '../Menu';
import { Button } from '../Button';

const Wrapper = styled.div`
  padding: 4rem;
  margin: auto;
`;

const MessageContent = styled.div`
  display: flex;
  place-items: center;
  place-content: center;
  flex-direction: column;
  gap: 1rem;
  padding: 2rem 1rem;

  svg {
    font-size: 4rem;
    margin: auto;
    opacity: var(--opacity-high);
    color: var(--link-color);
  }

  span {
    color: var(--body-text-color---opacity-med);
    text-align: center;
    display: block;
  }
`;

export default {
  title: 'Components/Overlay',
  component: Overlay,
  argTypes: {},
  parameters: {
    layout: 'centered'
  }
};

const Template = (args) => <Wrapper><Overlay {...args} /></Wrapper>;

export const Basic = Template.bind({});
Basic.args = {
  children: 'Overlay',
};

export const Message = Template.bind({});
Message.args = {
  children: (<MessageContent>
    <Create />
    <span>Write something insightful today</span>
  </MessageContent>),
};

export const AroundAMenu = Template.bind({});
AroundAMenu.args = {
  children: <Menu>
    <Button>Menu Item 1</Button>
    <Button>Menu Item 2</Button>
    <Button>Menu Item 3</Button>
  </Menu>,
};
