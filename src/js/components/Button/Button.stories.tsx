import styled from 'styled-components';

import { Button } from './Button';
import { BADGE, Storybook } from '../../storybook';

const Wrapper = styled.div`
  display: flex;
  gap: 1rem;
`;

export default {
  title: 'Components/Button',
  component: Button,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.BETA]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

const Template = (args) => <Button {...args} />;

export const Default = Template.bind({});
Default.args = {
  children: 'Button',
};


export const isPrimary = Template.bind({});
isPrimary.args = {
  isPrimary: false,
  children: 'Button',
};


export const Pressed = Template.bind({});
Pressed.args = {
  isPressed: true,
  children: 'Button',
};

const ThemeWrapper = styled.div`
  padding: 1rem;
  border-radius: 1rem;
  max-width: max-content;
  margin: auto;
  border: 1px solid var(--border-color);
  margin: 0.5rem auto;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

export const Variants = () => <>
  <ThemeWrapper className="is-theme-light">
    <Wrapper>
      <Button variant="plain">Plain</Button>
      <Button variant="gray">Gray</Button>
      <Button variant="tinted">Tinted</Button>
      <Button variant="filled">Filled</Button>
      <Button shape="round" variant="plain">Plain</Button>
      <Button shape="round" variant="gray">Gray</Button>
      <Button shape="round" variant="tinted">Tinted</Button>
      <Button shape="round" variant="filled">Filled</Button>
    </Wrapper>
    <Wrapper>
      <Button isPressed={true} variant="plain">Plain</Button>
      <Button isPressed={true} variant="gray">Gray</Button>
      <Button isPressed={true} variant="tinted">Tinted</Button>
      <Button isPressed={true} variant="filled">Filled</Button>
      <Button isPressed={true} shape="round" variant="plain">Plain</Button>
      <Button isPressed={true} shape="round" variant="gray">Gray</Button>
      <Button isPressed={true} shape="round" variant="tinted">Tinted</Button>
      <Button isPressed={true} shape="round" variant="filled">Filled</Button>
    </Wrapper>
  </ThemeWrapper>
  <ThemeWrapper className="is-theme-dark">
    <Wrapper>
      <Button variant="plain">Plain</Button>
      <Button variant="gray">Gray</Button>
      <Button variant="tinted">Tinted</Button>
      <Button variant="filled">Filled</Button>
      <Button shape="round" variant="plain">Plain</Button>
      <Button shape="round" variant="gray">Gray</Button>
      <Button shape="round" variant="tinted">Tinted</Button>
      <Button shape="round" variant="filled">Filled</Button>
    </Wrapper>
    <Wrapper>
      <Button isPressed={true} variant="plain">Plain</Button>
      <Button isPressed={true} variant="gray">Gray</Button>
      <Button isPressed={true} variant="tinted">Tinted</Button>
      <Button isPressed={true} variant="filled">Filled</Button>
      <Button isPressed={true} shape="round" variant="plain">Plain</Button>
      <Button isPressed={true} shape="round" variant="gray">Gray</Button>
      <Button isPressed={true} shape="round" variant="tinted">Tinted</Button>
      <Button isPressed={true} shape="round" variant="filled">Filled</Button>
    </Wrapper>
  </ThemeWrapper></>;


export const Icon = Template.bind({});
Icon.args = {
  children: 
    <svg width="24" height="24" strokeWidth="1.5" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M9 12H12M15 12H12M12 12V9M12 12V15" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
    <path d="M21 3.6V20.4C21 20.7314 20.7314 21 20.4 21H3.6C3.26863 21 3 20.7314 3 20.4V3.6C3 3.26863 3.26863 3 3.6 3H20.4C20.7314 3 21 3.26863 21 3.6Z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"/>
  </svg>,
};

