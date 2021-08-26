import styled from 'styled-components';
import { Button } from '../../Button';

export const RefsCount = styled(Button)`
  grid-area: refs;
  margin-left: 1em;
  z-index: var(--zindex-dropdown);
  appearance: none;
`;

export const Refs = ({ refsCount }) => (
  <RefsCount
    isPrimary={true}
  >
    {refsCount}
  </RefsCount>
);