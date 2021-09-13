import React from 'react';
import { BADGE, Storybook } from '@/utils/storybook';

import { Checkbox } from './Checkbox';

import { Meter } from '@/concept/Meter';

export default {
  title: 'Concepts/Checkbox',
  component: Checkbox,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
};

export const Basic = () => <Checkbox onChange={console.log}>Build Athens</Checkbox>;
export const Indeterminate = () => <Checkbox isIndeterminate onChange={console.log}>Build Athens</Checkbox>;
export const DefaultSelected = () => <Checkbox defaultSelected onChange={console.log}>Build Athens</Checkbox>;
export const Emphasized = () => <Checkbox isEmphasized onChange={console.log}>Build Athens</Checkbox>;
export const Disabled = () => <Checkbox isDisabled onChange={console.log}>Build Athens</Checkbox>;
export const Circle = () => <Checkbox styleCircle onChange={console.log}>Build Athens</Checkbox>;
export const StrikethroughWhenChecked = () => <Checkbox shouldStrikethroughWhenChecked onChange={console.log}>Build Athens</Checkbox>;

export const Checklist = () => {
  const [checked1, setChecked1] = React.useState(false);
  const [checked2, setChecked2] = React.useState(false);
  const [checked3, setChecked3] = React.useState(false);
  const [numChecked, setNumChecked] = React.useState(0)

  React.useEffect(() => {
    setNumChecked([checked1, checked2, checked3].filter(Boolean).length);
  }, [checked1, checked2, checked3])

  return (<>
    <Meter value={numChecked} maxValue={3} showValueLabel={false} label="Checklist" />
    <ul style={{ listStyle: 'none', padding: 0 }}>
      <li><Checkbox onChange={setChecked1} styleCircle shouldStrikethroughWhenChecked>Read How To Take Good Notes</Checkbox></li>
      <li><Checkbox onChange={setChecked2} styleCircle shouldStrikethroughWhenChecked>Build Athens</Checkbox></li>
      <li><Checkbox onChange={setChecked3} styleCircle shouldStrikethroughWhenChecked>Take a nap</Checkbox></li>
    </ul>
  </>
  )
};