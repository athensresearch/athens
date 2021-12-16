import React from 'react'
import { Input } from './Input';
import { BADGE, Storybook } from '@/utils/storybook';
import { Mail, Check } from '@material-ui/icons';
import styled from 'styled-components';

const InputStoryWrapper = styled(Storybook.Wrapper)`
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 1rem 2rem;

  hr {
    grid-column: 1 / -1;
    width: 100%;
    border: 0 0 1px;
    opacity: var(--opacity-low);
  }
`;

export default {
  title: 'Components/Input',
  component: Input,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => <InputStoryWrapper><Story /></InputStoryWrapper>]
};

const Template = (args) => <Input {...args} />;

export const Default = Template.bind({});
Default.args = {
  defaultValue: 'Input',
  type: 'text'
};

export const Password = () => <>
  <Input type="password" />
  <Input type="password" defaultValue="with a password" />
  <Input type="password" pattern="[A-Za-z]{3}" defaultValue="with a password" />
  <hr />
  <Input disabled type="password" />
  <Input disabled type="password" defaultValue="with a password" />
  <Input disabled type="password" pattern="[A-Za-z]{3}" defaultValue="with a password" />
</>

export const WithLayout = () => {
  return <>
    <Input.LabelWrapper>
      <Mail className="icon-left" />
      <Check className="icon-right" />
      <Input.Label>Label</Input.Label>
      <Input type="text" />
      <Input.Help>Help text</Input.Help>
    </Input.LabelWrapper>
  </>
}

export const WithValidation = () => {
  const [value, setValue] = React.useState(null);

  const regex = /^[_a-zA-Z0-9-+_.]+@[a-zA-Z0-9-\.]+(\.[a-zA-Z]{2,3})$/;
  const isValid = regex.exec(value);

  return <>
    <Input.LabelWrapper>
      <Mail className="icon-left" />
      {isValid && <Check className="icon-right" />}
      <Input.Label>Email Address</Input.Label>
      <Input required type="email" className={isValid ? 'is-valid' : 'is-invalid'} onKeyDown={(e) => setValue(e.target.value)} />
      <Input.Help>Provide a valid email</Input.Help>
    </Input.LabelWrapper>
  </>
}