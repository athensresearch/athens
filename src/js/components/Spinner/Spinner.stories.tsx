import { BADGE, Storybook } from '@/utils/storybook';

import { Spinner, Progress } from '@/Spinner/Spinner';
import { Indeterminate } from '@/Spinner/components/Indeterminate';

export default {
  title: 'Components/Spinner',
  component: Spinner,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story, args) => Template(Story, args)]
};

const Template = (Story, args) => <Storybook.Wrapper><Story {...args} /></Storybook.Wrapper>;

export const Basic = () => <Spinner />;
export const ProgressSpinner = () => <Progress />;
export const IndeterminateProgressSpinner = () => <>
  <Indeterminate style={{ "--size": "6rem" }} />
  <Indeterminate style={{ "--size": "5rem" }} />
  <Indeterminate style={{ "--size": "4rem" }} />
  <Indeterminate style={{ "--size": "3rem" }} />
  <Indeterminate style={{ "--size": "32px" }} />
  <Indeterminate style={{ "--size": "31px" }} />
  <Indeterminate style={{ "--size": "30px" }} />
  <Indeterminate style={{ "--size": "29px" }} />
  <Indeterminate style={{ "--size": "28px" }} />
  <Indeterminate style={{ "--size": "27px" }} />
  <Indeterminate style={{ "--size": "26px" }} />
  <Indeterminate style={{ "--size": "25px" }} />
  <Indeterminate style={{ "--size": "24px" }} />
  <Indeterminate style={{ "--size": "23px" }} />
  <Indeterminate style={{ "--size": "22px" }} />
  <Indeterminate style={{ "--size": "21px" }} />
  <Indeterminate style={{ "--size": "20px" }} />
  <Indeterminate style={{ "--size": "19px" }} />
  <Indeterminate style={{ "--size": "18px" }} />
  <Indeterminate style={{ "--size": "17px" }} />
  <Indeterminate style={{ "--size": "16px" }} />
  <Indeterminate style={{ "--size": "15px" }} />
  <Indeterminate style={{ "--size": "14px" }} />
  <Indeterminate style={{ "--size": "13px" }} />
  <Indeterminate style={{ "--size": "12px" }} />
  <Indeterminate style={{ "--size": "11px" }} />
  <Indeterminate style={{ "--size": "10px" }} />
  <Indeterminate style={{ "--size": "9px" }} />
  <Indeterminate style={{ "--size": "8px" }} />
</>;
