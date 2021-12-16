import styled from 'styled-components';

const Message = styled.span`
  color: var(--body-text-color---opacity-med);
`;

export const EmptyMessage = () => <Message>This page has no content.</Message>;