import styled from 'styled-components';

import { Button } from '../../Button';
import { Avatar } from '../../Avatar';

const MemberCount = styled.span`
  margin-left: 0.25rem;
`;

interface PresenceDetailsIndicatorProps {
  setPresenceDetailsAnchor: React.Dispatch<any>,
  setIsPresenceDetailsOpen: React.Dispatch<React.SetStateAction<boolean>>,
  isPresenceDetailsOpen: boolean,
  currentPageMembers: PersonPresence[],
  differentPageMembers: PersonPresence[],
}

export const PresenceDetailsIndicator = ({
  isPresenceDetailsOpen,
  setIsPresenceDetailsOpen,
  setPresenceDetailsAnchor,
  currentPageMembers,
  differentPageMembers,
}: PresenceDetailsIndicatorProps) => {
  const maxToDisplay = 5;
  const showablePersons = [...currentPageMembers, ...differentPageMembers];

  return <div ref={setPresenceDetailsAnchor}>
    <Button
      onClick={() => setIsPresenceDetailsOpen(!isPresenceDetailsOpen)}
      style={{ borderRadius: "100em" }}
      isPressed={isPresenceDetailsOpen}>
      <Avatar.Stack
        size="1em"
        maskSize="1.5px"
        overlap="0.2"
      >
        {showablePersons && showablePersons.slice(0, maxToDisplay).map((member, index) => {
          if (index < maxToDisplay) {
            return (
              <Avatar
                username={member.username}
                color={member.color}
                personId={member.personId}
                showTooltip={false}
              />
            );
          }
          return null;
        })}
      </Avatar.Stack>
      {showablePersons.length > maxToDisplay && <MemberCount>+{showablePersons.length - maxToDisplay}</MemberCount>}
    </Button>
  </div>;
}

