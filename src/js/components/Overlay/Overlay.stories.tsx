import styled from 'styled-components';
import { Create } from '@material-ui/icons';
import { BADGE, Storybook } from '../../storybook';

import { Overlay } from './Overlay';
import { Menu } from '../Menu';
import { Button } from '../Button';

export default {
  title: 'Components/Overlay',
  component: Overlay,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV, BADGE.IN_USE]
  }
};

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

const Template = (args) => <Storybook.Wrapper><Overlay {...args} /></Storybook.Wrapper>;

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
