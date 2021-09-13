import styled from 'styled-components';
import { Badge } from './Badge';
import { BADGE, Storybook } from '../../../storybook';

import { Home } from '@material-ui/icons';

export default {
  title: 'Components/Badge',
  component: Badge,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper style={{ gap: "2rem" }}><Story /></Storybook.Wrapper>]
};

const Wrapper = styled.div`
  display: flex;
  gap: 2rem;
  font-size: 2em;
`;

const MockObject = styled.div`
  border-radius: 0.25rem;
  background: var(--body-text-color---opacity-low);
  width: 2em;
  height: 2em;
`;

const Template = (args) => <Badge {...args} />;

export const Default = Template.bind({});
Default.args = {
  children: <MockObject />,
};

export const Content = Template.bind({});
Content.args = {
  children: <MockObject />,
  badgeContent: '7'
};

const StyledIcon = styled(Home)`
  background: var(--background-plus-2);
  padding: 0.125em;
  border-radius: 100em;
  font-size: 2.5rem !important;
`;

export const Position = () => <Wrapper>
  <Badge
    badgeContent="7"
    placement="top-left"
  >
    <StyledIcon />
  </Badge>
  <Badge
    badgeContent="7"
    placement="top-right"
  >
    <StyledIcon />
  </Badge>
  <Badge
    badgeContent="7"
    placement="bottom-right"
  >
    <StyledIcon />
  </Badge>
  <Badge
    badgeContent="7"
    placement="bottom-left"
  >
    <StyledIcon />
  </Badge>
</Wrapper>