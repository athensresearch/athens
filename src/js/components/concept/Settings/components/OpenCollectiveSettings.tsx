import React from 'react';
import styled from 'styled-components';

import { Check, Mail } from '@material-ui/icons';

import { Button } from '@/Button';
import { Input } from '@/Input';
import * as Setting from './Setting';

const Body = styled(Setting.Body)`
  align-items: flex-start;
  flex-direction: column;
  display: flex;
  gap: 0.5rem;
`;

const InputGroup = styled.div`
  display: flex;
`;

export const OpenCollectiveSettings = ({
  openCollectiveEmail,
  handleUpdateEmail,
  isUpdatingEmail,
}) => {
  const [fieldValue, setFieldValue] = React.useState(openCollectiveEmail);
  const [hasEmailChanged, setHasEmailChanged] = React.useState(false);
  const emailInputRef = React.useRef<HTMLInputElement>(null);

  const handleResetField = () => {
    setFieldValue(openCollectiveEmail);
    emailInputRef.current.value = openCollectiveEmail;
  }

  React.useEffect(() => {
    setHasEmailChanged(openCollectiveEmail !== fieldValue);
  }, [openCollectiveEmail, fieldValue, setHasEmailChanged]);

  return (
    <Setting.Wrap>
      <Setting.Header>
        <Setting.Title>OpenCollective Email</Setting.Title>
        <Setting.Glance>{!!openCollectiveEmail
          ? (<><Check /> {openCollectiveEmail}</>)
          : 'No email set.'}
        </Setting.Glance>
      </Setting.Header>
      <Body>
        <InputGroup>
          <Input.LabelWrapper>
            <Mail className="icon-left" />
            <Button
              style={{ visibility: hasEmailChanged ? 'visible' : 'hidden' }}
              disabled={!hasEmailChanged}
              shape="unset"
              variant="unset"
              className="input-right"
              onClick={handleResetField}
            >Reset</Button>
            <Input
              type="email"
              disabled={isUpdatingEmail}
              placeholder="OpenCollective Email"
              defaultValue={openCollectiveEmail}
              ref={emailInputRef}
              onChange={(e) => setFieldValue(e.target.value)}
              style={{ paddingRight: '4em' }}
            />
          </Input.LabelWrapper>
          <Button
            variant={!hasEmailChanged ? 'plain' : 'filled'}
            disabled={!hasEmailChanged}
            onClick={(fieldValue) => handleUpdateEmail(fieldValue)}
          >Save</Button>
        </InputGroup>
        <Setting.Details>
          <p>{!!openCollectiveEmail ? "Thank you for supporting Athens! Backups are coming soon." : "You are using the free version of Athens. You are hosting your own data. Please be careful!"}</p>
        </Setting.Details>
      </Body>
    </Setting.Wrap>
  )
};