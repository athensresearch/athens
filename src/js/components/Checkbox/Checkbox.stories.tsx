import { Checkbox } from './Checkbox';
import { BADGE, Storybook } from '../../storybook';

export default {
  title: 'components/Checkbox',
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

export const Checklist = () => (
  <ul style={{ listStyle: 'none' }}>
    <li><Checkbox styleCircle shouldStrikethroughWhenChecked>Read How To Take Good Notes</Checkbox></li>
    <li><Checkbox styleCircle shouldStrikethroughWhenChecked>Read What's Best Next</Checkbox></li>
    <li><Checkbox styleCircle shouldStrikethroughWhenChecked>Build Athens</Checkbox></li>
    <li><Checkbox styleCircle shouldStrikethroughWhenChecked>Take a nap</Checkbox></li>
  </ul>
);