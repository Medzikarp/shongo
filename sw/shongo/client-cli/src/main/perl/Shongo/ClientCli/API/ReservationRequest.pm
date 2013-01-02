#
# Reservation request
#
# @author Martin Srom <martin.srom@cesnet.cz>
#
package Shongo::ClientCli::API::ReservationRequest;
use base qw(Shongo::ClientCli::API::ReservationRequestNormal);

use strict;
use warnings;

use Shongo::Common;
use Shongo::Console;
use Shongo::ClientCli::API::Specification;

# Enumeration of state
our $State = ordered_hash(
    'NOT_COMPLETE' => 'Not Complete',
    'NOT_ALLOCATED' => 'Not Allocated',
    'ALLOCATED' => 'Allocated',
    'ALLOCATION_FAILED' => 'Allocation Failed',
    'STARTED' => 'Started',
    'STARTING_FAILED' => 'Starting Failed',
    'FINISHED' => 'Finished'
);

#
# Create a new instance of reservation request
#
# @static
#
sub new()
{
    my $class = shift;
    my (%attributes) = @_;
    my $self = Shongo::ClientCli::API::ReservationRequestNormal->new(@_);
    bless $self, $class;

    $self->set_object_class('ReservationRequest');
    $self->set_object_name('Reservation Request');

    $self->add_attribute('slot', {
        'title' => 'Requested Slot',
        'type' => 'interval',
        'complex' => 1,
        'required' => 1
    });
    $self->add_attribute('specification', {
        'complex' => 1,
        'modify' => sub {
            my ($specification) = @_;
            my $class = undef;
            if ( defined($specification) ) {
                $class = $specification->{'class'};
            }
            $class = Shongo::ClientCli::API::Specification::select_type($class);
            if ( !defined($specification) || !($class eq $specification->get_object_class()) ) {
                $specification = Shongo::ClientCli::API::Specification->create({'class' => $class});
            } else {
                $specification->modify();
            }
            return $specification;
        },
        'required' => 1
    });
    $self->add_attribute('state', {
        'title' =>'Current State',
        'format' => sub {
            my $state = $self->get_state();
            if ( defined($state) ) {
                if ( defined($self->get('state')) && $self->get('state') eq 'ALLOCATED' ) {
                    $state .= sprintf(" (" . colored("reservation", $Shongo::ClientCli::API::Object::COLOR) . ": %s)", $self->{'reservationId'});
                }
                my $color = 'blue';
                if ( defined($self->get('state')) && $self->get('state') eq 'ALLOCATION_FAILED' ) {
                    $color = 'red';
                }
                my $state_report = $self->{'stateReport'};
                $state_report = format_report($state_report, get_term_width() - 23);
                $state .= "\n" . colored($state_report, $color);
                return $state;
            }
            return undef;
        },
        'read-only' => 1
    });
    $self->add_attribute_preserve('reservationId');
    $self->add_attribute_preserve('stateReport');

    return $self;
}

#
# @return state
#
sub get_state
{
    my ($self) = @_;
    if ( !defined($self->{'state'}) ) {
        return undef;
    }
    my $state = $State->{$self->{'state'}};
    if ( $self->{'state'} eq 'NOT_COMPLETE' ) {
        $state = colored($state, 'yellow')
    }
    elsif ( $self->{'state'} eq 'ALLOCATED' ) {
        $state = colored($state, 'green')
    }
    elsif ( $self->{'state'} eq 'ALLOCATION_FAILED' ) {
        $state = colored($state, 'red')
    }
    else {
        $state = colored($state, 'blue');
    }
    return '[' . $state . ']';
}

1;