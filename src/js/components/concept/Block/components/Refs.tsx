import styled from 'styled-components';
import { Button } from '@/Button';

export const RefsCount = styled(Button)`
  grid-area: refs;
  margin-left: 1em;
  padding: 0.25rem;
  z-index: var(--zindex-dropdown);
  appearance: none;
  align-self: flex-start;
  justify-self: flex-start;
  font-size: 85%;
`;

export const Refs = ({ refsCount, ...props }) => (
  <RefsCount
    isPrimary={true}
    {...props}
  >
    {refsCount >= 100 ? "99+" : refsCount}
  </RefsCount>
);